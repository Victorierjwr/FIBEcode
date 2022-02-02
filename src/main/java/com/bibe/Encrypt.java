package com.bibe;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.util.Arrays;
import java.util.Base64;
import java.util.Properties;

import static com.utils.FileUtils.loadPropFromFile;
import static com.utils.FileUtils.storePropToFile;

public class Encrypt {

    /**
     * 输入明文消息 message 消息的属性集 messageAttrList
     */
    private final Element message;
    private final int[] messageAttrList;

    public Encrypt(Element message, int[] messageAttrList) {
        this.message = message;
        this.messageAttrList = messageAttrList;
    }

    public void encrypt(String ctFileName){
        Pairing bp = Setup.bp;
        Properties pkProp = Setup.pkProp;

        //密文第一个部分
        Element egg_y = bp.getGT().newElementFromBytes(Base64.getDecoder().decode(pkProp.getProperty("egg_y"))).getImmutable();
        //计算密文组件 Me(g,g)^ys
        Element s = bp.getZr().newRandomElement().getImmutable();
        Element EP = message.duplicate().mul(egg_y.powZn(s)).getImmutable();

        Properties ctProp = new Properties();
        //针对每个密文属性，计算密文组件 E=T^s
        for (int att : messageAttrList) {
            String TString = pkProp.getProperty("T"+att);
            Element T = bp.getG1().newElementFromBytes(Base64.getDecoder().decode(TString)).getImmutable();
            Element E = T.powZn(s).getImmutable();

            ctProp.setProperty("E"+att, Base64.getEncoder().withoutPadding().encodeToString(E.toBytes()));
        }
        ctProp.setProperty("EP", Base64.getEncoder().withoutPadding().encodeToString(EP.toBytes()));
        //密文属性列表也添加至密文中
        ctProp.setProperty("messageAttrList", Arrays.toString(messageAttrList));
        storePropToFile(ctProp, ctFileName);
    }

}
