package com.bibe;

import it.unisa.dia.gas.jpbc.Element;
import org.junit.Test;
import sun.util.resources.el.CalendarData_el_CY;

import java.security.Key;

public class testDemo {

    int U = 20;
    int d = 5;

    int[] userAttList = {1, 5, 3, 6, 10, 11};
    int[] messageAttList = {1,  3,  5,  7, 9, 10, 11};

    String dir = "src/test/resources/data/";
    String pairingParametersFileName = "com/ibe/params/curves/a.properties";
    String pkFileName = dir + "pk.properties";
    String mskFileName = dir + "msk.properties";
    String skFileName = dir + "sk.properties";
    String ctFileName = dir + "ct.properties";

    @Test
    public void testSetup(){
        Setup setup = new Setup(pairingParametersFileName,U,d,pkFileName,mskFileName);
        setup.setup();
    }

    @Test
    public void testKeyGen(){
        Setup setup = new Setup(pairingParametersFileName,U,d,pkFileName,mskFileName);
        setup.setup();

        KeyGen keyGen = new KeyGen(mskFileName,skFileName,userAttList);
        keyGen.keygen();
    }

    @Test
    public void testEncrypt(){
        Setup setup = new Setup(pairingParametersFileName,U,d,pkFileName,mskFileName);
        setup.setup();
        KeyGen keyGen = new KeyGen(mskFileName,skFileName,userAttList);
        keyGen.keygen();

        Element message = Setup.bp.getGT().newElement().getImmutable();
        System.out.println("明文消息：" + message);
        Encrypt encrypt = new Encrypt(message,messageAttList);
        encrypt.encrypt(ctFileName);
    }

    @Test
    public void testDecrypt(){
        Setup setup = new Setup(pairingParametersFileName,U,d,pkFileName,mskFileName);
        setup.setup();
        KeyGen keyGen = new KeyGen(mskFileName,skFileName,userAttList);
        keyGen.keygen();
        Element message = Setup.bp.getGT().newElement().getImmutable();
        System.out.println("明文消息：" + message);
        Encrypt encrypt = new Encrypt(message,messageAttList);
        encrypt.encrypt(ctFileName);

        Decrypt decrypt = new Decrypt(ctFileName,skFileName);
        Element res = decrypt.decrypt();
        System.out.println("恢复后明文为：" + res);
    }

}
