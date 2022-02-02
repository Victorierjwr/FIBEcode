package com.bibe;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.util.Arrays;
import java.util.Base64;
import java.util.Properties;

import static com.utils.FileUtils.loadPropFromFile;

public class Decrypt {

    private String ctFileName;
    private String skFileName;

    public Decrypt(String ctFileName, String skFileName) {
        this.ctFileName = ctFileName;
        this.skFileName = skFileName;
    }

    public Element decrypt(){
        Pairing bp = Setup.bp;
        int d = Setup.d;

        Properties ctProp = loadPropFromFile(ctFileName);
        String messageAttListString = ctProp.getProperty("messageAttrList");
        //恢复明文消息的属性列表 int[]类型
        int[] messageAttrList = Arrays.stream(messageAttListString.substring(1, messageAttListString.length()-1).split(",")).map(String::trim).mapToInt(Integer::parseInt).toArray();

        Properties skProp = loadPropFromFile(skFileName);
        String userAttrListString = skProp.getProperty("userAttrList");
        //恢复用户属性列表 int[]类型
        int[] userAttList = Arrays.stream(userAttrListString.substring(1, userAttrListString.length()-1).split(",")).map(String::trim).mapToInt(Integer::parseInt).toArray();

        //判断两个列表重合个数是否小于d
        int[] intersectionAttList = intersection(messageAttrList, userAttList);
        System.out.println("重合属性列表：" + Arrays.toString(intersectionAttList));
        System.out.println("重合属性个数为：" + intersectionAttList.length);
        if (intersectionAttList.length < d) {
            System.out.println("不满足解密门限，无法解密！");
            return null;
        }
        //从两个列表中的重合项中取前d项，构成解密属性列表
        int[] decAttList = Arrays.copyOfRange(intersectionAttList, 0, d);
        System.out.println("解密所用属性列表：" + Arrays.toString(decAttList));

        /**
         * 如果满足揭秘条件，开始解密过程
         */
        //denominator用于记录连乘的双线性对
        Element denominator = bp.getGT().newOneElement().getImmutable();
        //针对解密属性列表中的每个属性，计算e(D,E)^delta，并将结果连乘
        for (int att : decAttList){
            String EString = ctProp.getProperty("E"+att);
            Element E = bp.getG1().newElementFromBytes(Base64.getDecoder().decode(EString)).getImmutable();

            String DString = skProp.getProperty("D"+att);
            Element D = bp.getG1().newElementFromBytes(Base64.getDecoder().decode(DString)).getImmutable();

            //计算属性对应的拉格朗日因子，作为指数。目标值x为0。
            Element delta = lagrange(att, decAttList, 0, bp.getZr()).getImmutable();
            denominator = denominator.mul(bp.pairing(E,D).powZn(delta));
        }

        String EPString = ctProp.getProperty("EP");
        Element EP = bp.getGT().newElementFromBytes(Base64.getDecoder().decode(EPString)).getImmutable();
        //恢复M=EP除以上述连乘结果
        return EP.div(denominator);
    }

    //计算由coef为系数确定的多项式qx在点x处的值，注意多项式计算在群Zr上进行
    public static Element qx(Element x, Element[] coef, Field Zr){
        Element res = coef[0];
        for (int i = 1; i < coef.length; i++){
            Element exp = Zr.newElement(i).getImmutable();
            //x一定要使用duplicate复制使用，因为x在每一次循环中都要使用，如果不加duplicte，x的值会发生变化
            res = res.add(coef[i].mul(x.duplicate().powZn(exp)));
        }
        return res;
    }

    //求两个数组的交集
    public static int[] intersection(int[] nums1, int[] nums2) {
        Arrays.sort(nums1);
        Arrays.sort(nums2);
        int length1 = nums1.length, length2 = nums2.length;
        int[] intersection = new int[length1 + length2];
        int index = 0, index1 = 0, index2 = 0;
        while (index1 < length1 && index2 < length2) {
            int num1 = nums1[index1], num2 = nums2[index2];
            if (num1 == num2) {
                // 保证加入元素的唯一性
                if (index == 0 || num1 != intersection[index - 1]) {
                    intersection[index++] = num1;
                }
                index1++;
                index2++;
            } else if (num1 < num2) {
                index1++;
            } else {
                index2++;
            }
        }
        return Arrays.copyOfRange(intersection, 0, index);
    }

    //拉格朗日因子计算 i是集合S中的某个元素，x是目标点的值
    public static Element lagrange(int i, int[] S, int x, Field Zr) {
        Element res = Zr.newOneElement().getImmutable();
        Element iElement = Zr.newElement(i).getImmutable();
        Element xElement = Zr.newElement(x).getImmutable();
        for (int j : S) {
            if (i != j) {
                //注意：在循环中重复使用的项一定要用duplicate复制出来使用
                //这儿xElement和iElement重复使用，但因为前面已经getImmutable所以可以不用duplicate
                Element numerator = xElement.sub(Zr.newElement(j));
                Element denominator = iElement.sub(Zr.newElement(j));
                res = res.mul(numerator.div(denominator));
            }
        }
        return res;
    }

}
