package com.bibe;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Properties;

import static com.utils.FileUtils.loadPropFromFile;
import static com.utils.FileUtils.storePropToFile;

public class KeyGen {

    protected String mskFileName;
    protected String skFileName;
    protected int[] userAttrList;

    public KeyGen(String mskFileName,String skFileName,int[] userAttrList){
        this.mskFileName = mskFileName;
        this.skFileName = skFileName;
        this.userAttrList = userAttrList;
    }

    public void keygen(){
        Pairing bp = Setup.bp;

        Element g = Setup.g; int U = Setup.U; int d = Setup.d;

        //从配置文件中获取主私钥
        Properties mskProp = loadPropFromFile(mskFileName);
        String y_Str = mskProp.getProperty("y");
        Element y = bp.getZr().newElementFromBytes(Base64.getDecoder().decode(y_Str)).getImmutable();

        /**
         * 生成拉格朗日多项式
         * d-1次多项式表示为q(x)=Lagrange[0] + Lagrange[1]*x^1 + Lagrange[2]*x^2 + Lagrange[d-1]*x^(d-1)
         * 多项式的系数的数据类型为Zr Element，从而是的后续相关计算全部在Zr群上进行
         * 通过随机选取Lagrange参数，来构造d-1次多项式q(x)。约束条件为q(0)=y。
         */
        Element[] Lagrange = new Element[d];
        Lagrange[0] = y;
        for (int i = 1; i < d; i++){
            Lagrange[i] = bp.getZr().newRandomElement().getImmutable();
        }

        Properties skProp = new Properties();
        //计算每个属性对应的私钥g^(q/t)，q是多项式在该属性位置的值，t是属性对应的主密钥
        for (int att : userAttrList) {
            String tString = mskProp.getProperty("t"+att);
            Element t = bp.getZr().newElementFromBytes(Base64.getDecoder().decode(tString)).getImmutable();
            Element q = qx(bp.getZr().newElement(att), Lagrange).getImmutable();
            Element D = g.powZn(q.div(t)).getImmutable();

            skProp.setProperty("D"+att, Base64.getEncoder().withoutPadding().encodeToString(D.toBytes()));
        }
        //将用户属性列表也添加在私钥中
        skProp.setProperty("userAttrList", Arrays.toString(userAttrList));
        storePropToFile(skProp, skFileName);

    }

    //计算由Lagrange为系数确定的多项式qx在点x处的值，注意多项式计算在群Zr上进行
    public static Element qx(Element x, Element[] Lagrange){
        Element res = Lagrange[0];
        for (int i = 1; i < Lagrange.length; i++){
            Element exp = Setup.bp.getZr().newElement(i).getImmutable();
            //x一定要使用duplicate复制使用，因为x在每一次循环中都要使用，如果不加duplicate，x的值会发生变化
            res = res.add(Lagrange[i].mul(x.duplicate().powZn(exp)));
        }
        return res;
    }

}
