package com.bibe;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.util.Base64;
import java.util.Properties;

import static com.utils.FileUtils.storePropToFile;

/**
 * 初始化阶段:
 * 1.生成安全参数 <e(),g,G1,GT,Zr>
 * 2.给定属性全集整数集合U={1,2,3,...,|U|},以及系统陷门值d
 * 3.对每个属性随机选取t_i作为主密钥组件，同时生成 Ti = g^t_i 作为公钥组件
 * 4.随机选取y作为主密钥主键，生成Y = e(g,g)^y 为公钥组件
 * 5.输出msk={t1,t2,...,t_|U|, y}, 公钥pk={T1,T2,T3,...,T_|U|, Y}
 */

public class Setup {

    /**
     * 初始化阶段输入：公共参数（参数值）
     */
    public String pairingParametersFileName;
    public String pkFileName;
    public String mskFileName;

    /**
     * 公共参数，可公开的
     */
    public static Pairing bp;
    public static Element g;
    public static Properties pkProp;

    public static int U;
    public static int d;



    public Setup(){}

    public Setup(String pairingParametersFileName, int U, int d,String pkFileName, String mskFileName) {
        this.pairingParametersFileName = pairingParametersFileName;
        this.pkFileName = pkFileName;
        this.mskFileName = mskFileName;
        Setup.U = U;
        Setup.d = d;
    }


    public void setup(){
        bp = PairingFactory.getPairing(pairingParametersFileName);
        g = bp.getG1().newRandomElement().getImmutable();

        /**
         * 公钥可公开，主私钥不公开定义为局部变量，密钥生成从配置文件msk.properties中获取
         */
        pkProp = new Properties();
        Properties mskProp = new Properties();
        //属性表示为1，2，3，...，U
        //对每个属性i，选取一个随机数ti作为该属性对应的主密钥，并计算相应公钥g^ti
        for(int i = 1;i<= U;i++){
            Element t = bp.getZr().newRandomElement().getImmutable();
            Element T = g.powZn(t).getImmutable();
            mskProp.setProperty("t"+i, Base64.getEncoder().withoutPadding().encodeToString(t.toBytes()));
            pkProp.setProperty("T"+i, Base64.getEncoder().withoutPadding().encodeToString(T.toBytes()));
        }

        //选取随机数y，计算e(g,g)^y作为密钥组件另一部分
        Element y = bp.getZr().newRandomElement().getImmutable();
        Element egg_y = bp.pairing(g,g).powZn(y).getImmutable();
        mskProp.setProperty("y", Base64.getEncoder().withoutPadding().encodeToString(y.toBytes()));
        pkProp.setProperty("egg_y", Base64.getEncoder().withoutPadding().encodeToString(egg_y.toBytes()));

        //注意区分数据类型。上面写的数据类型群元素，因此使用了Base64编码。
        //d在实际应用中定义为一个int类型，直接用Integer.toString方法转字符串
        pkProp.setProperty("d", Integer.toString(d));

        storePropToFile(mskProp, mskFileName);
        storePropToFile(pkProp, pkFileName);
    }
}
