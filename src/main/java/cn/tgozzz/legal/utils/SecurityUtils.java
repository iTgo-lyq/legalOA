package cn.tgozzz.legal.utils;

import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Objects;

@Component
public class SecurityUtils {

    /**
     * 签名算法
     */
    public static final String SIGN_ALGORITHMS = "SHA1WithRSA";

    /**
     * SHA256摘要
     */
    public static String getSHA256(String str) {
        MessageDigest messageDigest;
        String encodestr = "";
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(str.getBytes(StandardCharsets.UTF_8));
            encodestr = MyUtils.byte2Hex(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return encodestr;
    }

    /**
     * 随机生成密钥对, 通过base64编码后写入到文件
     */
    public static void genKeyPair(String filePath) {
        // KeyPairGenerator类用于生成公钥和私钥对，基于RSA算法生成对象
        KeyPairGenerator keyPairGen = null;
        try {
            keyPairGen = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        // 初始化密钥对生成器，密钥大小为96-1024位
        assert keyPairGen != null;
        keyPairGen.initialize(1024, new SecureRandom());
        // 生成一个密钥对，保存在keyPair中
        KeyPair keyPair = keyPairGen.generateKeyPair();
        // 得到私钥
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        // 得到公钥
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        try {
            // 得到公钥字符串
            String publicKeyString = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            // 得到私钥字符串
            String privateKeyString = Base64.getEncoder().encodeToString(privateKey.getEncoded());
            // 将密钥对写入到文件
            FileWriter pubfw = new FileWriter(filePath + "/publicKey.keystore");
            FileWriter prifw = new FileWriter(filePath + "/privateKey.keystore");
            BufferedWriter pubbw = new BufferedWriter(pubfw);
            BufferedWriter pribw = new BufferedWriter(prifw);
            pubbw.write(publicKeyString);
            pribw.write(privateKeyString);
            pubbw.flush();
            pubbw.close();
            pubfw.close();
            pribw.flush();
            pribw.close();
            prifw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 从文件中中加载公钥
     */
    public static String loadPublicKeyByFile(String path) throws Exception {
        try {
            BufferedReader br = new BufferedReader(new FileReader(path
                    + "/publicKey.keystore"));
            String readLine = null;
            StringBuilder sb = new StringBuilder();
            while ((readLine = br.readLine()) != null) {
                sb.append(readLine);
            }
            br.close();
            return sb.toString();
        } catch (IOException e) {
            throw new Exception("公钥数据流读取错误");
        } catch (NullPointerException e) {
            throw new Exception("公钥输入流为空");
        }
    }

    /**
     * 从base64字符串加载公匙
     */
    public static RSAPublicKey loadPublicKeyByStr(String publicKeyStr)
            throws Exception {
        try {
            byte[] buffer = Base64.getDecoder().decode(publicKeyStr);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
            return (RSAPublicKey) keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此算法");
        } catch (InvalidKeySpecException e) {
            throw new Exception("公钥非法");
        } catch (NullPointerException e) {
            throw new Exception("公钥数据为空");
        }
    }

    /**
     * 从文件中加载私钥
     */
    public static String loadPrivateKeyByFile(String path) throws Exception {
        try {
            BufferedReader br = new BufferedReader(new FileReader(path
                    + "/privateKey.keystore"));
            String readLine = null;
            StringBuilder sb = new StringBuilder();
            while ((readLine = br.readLine()) != null) {
                sb.append(readLine);
            }
            br.close();
            return sb.toString();
        } catch (IOException e) {
            throw new Exception("私钥数据读取错误");
        } catch (NullPointerException e) {
            throw new Exception("私钥输入流为空");
        }
    }

    /**
     * 从base64字符串中加载私钥
     */
    public static RSAPrivateKey loadPrivateKeyByStr(String privateKeyStr)
            throws Exception {
        try {
            byte[] buffer = Base64.getDecoder().decode(privateKeyStr);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(buffer);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此算法");
        } catch (InvalidKeySpecException e) {
            throw new Exception("私钥非法");
        } catch (NullPointerException e) {
            throw new Exception("私钥数据为空");
        }
    }

    /**
     * 公钥加密过程
     *
     * @param publicKey     公钥
     * @param plainTextData 明文数据
     */
    public static byte[] encrypt(RSAPublicKey publicKey, byte[] plainTextData)
            throws Exception {
        if (publicKey == null) {
            throw new Exception("加密公钥为空, 请设置");
        }
        Cipher cipher;
        try {
            // 使用默认RSA
            cipher = Cipher.getInstance("RSA");
            // cipher= Cipher.getInstance("RSA", new BouncyCastleProvider());
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return cipher.doFinal(plainTextData);
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此加密算法");
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
            return null;
        } catch (InvalidKeyException e) {
            throw new Exception("加密公钥非法,请检查");
        } catch (IllegalBlockSizeException e) {
            throw new Exception("明文长度非法");
        } catch (BadPaddingException e) {
            throw new Exception("明文数据已损坏");
        }
    }

    public static String encryptToBase64(RSAPublicKey publicKey, byte[] plainTextData) throws Exception {
        return Base64.getEncoder().encodeToString(encrypt(publicKey, plainTextData));
    }

    /**
     * 私钥加密过程
     *
     * @param privateKey    私钥
     * @param plainTextData 明文数据
     */
    public static byte[] encrypt(RSAPrivateKey privateKey, byte[] plainTextData)
            throws Exception {
        if (privateKey == null) {
            throw new Exception("加密私钥为空, 请设置");
        }
        Cipher cipher;
        try {
            // 使用默认RSA
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            return cipher.doFinal(plainTextData);
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此加密算法");
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
            return null;
        } catch (InvalidKeyException e) {
            throw new Exception("加密私钥非法,请检查");
        } catch (IllegalBlockSizeException e) {
            throw new Exception("明文长度非法");
        } catch (BadPaddingException e) {
            throw new Exception("明文数据已损坏");
        }
    }

    /**
     * 私钥解密
     *
     * @param privateKey 私钥
     * @param cipherData 密文数据
     * @return 明文
     */
    public static byte[] decrypt(RSAPrivateKey privateKey, byte[] cipherData)
            throws Exception {
        if (privateKey == null) {
            throw new Exception("解密私钥为空, 请设置");
        }
        Cipher cipher;
        try {
            // 使用默认RSA
            cipher = Cipher.getInstance("RSA");
            // cipher= Cipher.getInstance("RSA", new BouncyCastleProvider());
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return cipher.doFinal(cipherData);
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此解密算法");
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
            return null;
        } catch (InvalidKeyException e) {
            throw new Exception("解密私钥非法,请检查");
        } catch (IllegalBlockSizeException e) {
            throw new Exception("密文长度非法");
        } catch (BadPaddingException e) {
            throw new Exception("密文数据已损坏");
        }
    }

    public static String decryptToString(RSAPrivateKey privateKey, byte[] cipherData) throws Exception {
        return new String(Objects.requireNonNull(decrypt(privateKey, cipherData)));
    }

    /**
     * 公钥解密过程
     *
     * @param publicKey  公钥
     * @param cipherData 密文数据
     * @return 明文
     */
    public static byte[] decrypt(RSAPublicKey publicKey, byte[] cipherData)
            throws Exception {
        if (publicKey == null) {
            throw new Exception("解密公钥为空, 请设置");
        }
        Cipher cipher;
        try {
            // 使用默认RSA
            cipher = Cipher.getInstance("RSA");
            // cipher= Cipher.getInstance("RSA", new BouncyCastleProvider());
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            return cipher.doFinal(cipherData);
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此解密算法");
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
            return null;
        } catch (InvalidKeyException e) {
            throw new Exception("解密公钥非法,请检查");
        } catch (IllegalBlockSizeException e) {
            throw new Exception("密文长度非法");
        } catch (BadPaddingException e) {
            throw new Exception("密文数据已损坏");
        }
    }

    /**
     * RSA签名
     */
    public static String sign(String content, String privateKey) {
        try {
            PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey));

            KeyFactory keyF = KeyFactory.getInstance("RSA");
            PrivateKey priKey = keyF.generatePrivate(priPKCS8);

            Signature signature = Signature.getInstance(SIGN_ALGORITHMS);

            signature.initSign(priKey);
            signature.update(content.getBytes(StandardCharsets.UTF_8));

            byte[] signed = signature.sign();

            return Base64.getEncoder().encodeToString(signed);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * RSA验签名检查
     */
    public static boolean doCheck(String content, String sign, String publicKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            byte[] encodedKey = Base64.getDecoder().decode(publicKey);
            PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));


            java.security.Signature signature = java.security.Signature
                    .getInstance(SIGN_ALGORITHMS);

            signature.initVerify(pubKey);
            signature.update(content.getBytes(StandardCharsets.UTF_8));

            return signature.verify(Base64.getDecoder().decode(sign));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static void main(String[] args) throws Exception {
//        System.out.println("--------------系统公钥加密 系统私钥解密-------------------");
//        String filePath1 = "D:/temp/system/";
//        SecurityUtils.genKeyPair(filePath1);
//        String plainText = "这是原文数据";
//        System.out.println("原文：" + plainText);
//        String cipher = SecurityUtils.encryptToBase64(SecurityUtils.loadPublicKeyByStr(SecurityUtils.loadPublicKeyByFile(filePath1)), plainText.getBytes());
//        System.out.println("加密：" + cipher);
//        String res = SecurityUtils.decryptToString(SecurityUtils.loadPrivateKeyByStr(SecurityUtils.loadPrivateKeyByFile(filePath1)), Base64.getDecoder().decode(cipher));
//        System.out.println("解密：" + res);
//        System.out.println();
//
//        System.out.println("--------------用户私钥签名 用户公钥验签-------------------");
//        String filePath2 = "D:/temp/user";
//        SecurityUtils.genKeyPair(filePath2);
//        String mainText = "合同正文是我";
//        System.out.println("正文：" + plainText);
//        String summaryInfo = SecurityUtils.getSHA256(mainText);
//        System.out.println("摘要：" + summaryInfo);
//        String signature = SecurityUtils.sign(summaryInfo, SecurityUtils.loadPrivateKeyByFile(filePath2));
//        System.out.println("签名：" + signature);
//        System.out.println("验签结果："+SecurityUtils.doCheck(summaryInfo, signature, SecurityUtils.loadPublicKeyByFile(filePath2)));
//        System.out.println();
        System.out.println(getSHA256("微泛设备租赁合同　（        ）租字           号　　出租方：中国工商银行××市信托部（简称甲方）　　承租方：                        （简称乙方）　　甲、乙双方根据“中国工商银行××市信托部设备租赁业务试行办法”的规定，签订设备租赁合同，并商定如下条款，共同遵守执行。　　一、甲方根据乙方上级批准的项目和乙方自行选定的设备和技术质量标准，向           购进以下设备租给乙方使用。　　１．                                                                　　２．                                                                　　３                                                                　　４．                                                                　　二、甲方根据与生产厂（商）签订的设备订货合同规定，于        年     季交货。由供货单位直接发运给乙方。乙方直接到供货单位自提自运。乙方收货后应立即向甲方开回设备收据。　　三、设备的验收、安装、调试、使用、保养、维修管理等，均由乙方自行负责。设备的质量问题由生产厂负责，并在订货合同予以说明。　　四、设备在租赁期间的所有权属于甲方。乙方收货后，应以甲方名义向当地保险公司投保综合险，保险费由乙方负责。乙方应将投保合同交甲方作为本合同附件。　　五、在租赁期内，乙方享有设备的使用权，但不得转让或作为财产抵押，未经甲方同意亦不得在设备上增加或拆除任何部件和迁移安装地点。甲方有权检查设备的使用和完好情况，乙方应提供一切方便。　　六、设备租赁期限为        年，租期从供货厂向甲方托收货款时算起，租金总额为人民币        元（包括手续费      ），分      期交付，每期租赁金         元，由甲方在每期期末按期向乙方托收。如乙方不能按期承付租金，甲方则按逾期租金总额每天加收万分之三的罚金。　　七、本合同一经签订不能撤销。如乙方提前交清租金，结束合同，甲方给予退还一部分利息的优惠。　　八、本合同期满，甲方同意按人民币          元的优惠价格将设备所有权转给乙方。　　九、乙方上级单位             同意作为乙方的经济担保人，负责乙方切实履行本合同各条款规定，如乙方在合同期内不能承担合同中规定的经济责任时，担保人应向甲方支付乙方余下的各期租金和其它损失。　　十、本合同经双方和乙方担保人盖章后生效。本合同正本两份，甲、乙方各执一份，副本      份，乙方担保人和乙方开户银行各一份。    甲方：中国工商银行××市信托部（公章）    负责人：             （签章）    开户银行及帐号：                             年     月     日    乙方：                        （全称）                                  （公章）    负责人：                 （签章）    开户银行及帐号：                                                     年     月     日    经济担保单位：                    （全称）    （公章）    负责人：                  （公章）                      年     月     日文档最终负责人（微泛）："));
        System.out.println(getSHA256("微泛设备租赁合同　（        ）租字           号　　出租方：中国工商银行××市信托部（简称甲方）　　承租方：                        （简称乙方）　　甲、乙双方根据“中国工商银行××市信托部设备租赁业务试行办法”的规定，签订设备租赁合同，并商定如下条款，共同遵守执行。　　一、甲方根据乙方上级批准的项目和乙方自行选定的设备和技术质量标准，向           购进以下设备租给乙方使用。　　１．                                                                　　２．                                                                　　３                                                                　　４．                                                                　　二、甲方根据与生产厂（商）签订的设备订货合同规定，于        年     季交货。由供货单位直接发运给乙方。乙方直接到供货单位自提自运。乙方收货后应立即向甲方开回设备收据。　　三、设备的验收、安装、调试、使用、保养、维修管理等，均由乙方自行负责。设备的质量问题由生产厂负责，并在订货合同予以说明。　　四、设备在租赁期间的所有权属于甲方。乙方收货后，应以甲方名义向当地保险公司投保综合险，保险费由乙方负责。乙方应将投保合同交甲方作为本合同附件。　　五、在租赁期内，乙方享有设备的使用权，但不得转让或作为财产抵押，未经甲方同意亦不得在设备上增加或拆除任何部件和迁移安装地点。甲方有权检查设备的使用和完好情况，乙方应提供一切方便。　　六、设备租赁期限为        年，租期从供货厂向甲方托收货款时算起，租金总额为人民币        元（包括手续费      ），分      期交付，每期租赁金         元，由甲方在每期期末按期向乙方托收。如乙方不能按期承付租金，甲方则按逾期租金总额每天加收万分之三的罚金。　　七、本合同一经签订不能撤销。如乙方提前交清租金，结束合同，甲方给予退还一部分利息的优惠。　　八、本合同期满，甲方同意按人民币          元的优惠价格将设备所有权转给乙方。　　九、乙方上级单位             同意作为乙方的经济担保人，负责乙方切实履行本合同各条款规定，如乙方在合同期内不能承担合同中规定的经济责任时，担保人应向甲方支付乙方余下的各期租金和其它损失。　　十、本合同经双方和乙方担保人盖章后生效。本合同正本两份，甲、乙方各执一份，副本      份，乙方担保人和乙方开户银行各一份。    甲方：中国工商银行××市信托部（公章）    负责人：             （签章）    开户银行及帐号：                             年     月     日    乙方：                        （全称）                                  （公章）    负责人：                 （签章）    开户银行及帐号：                                                     年     月     日    经济担保单位：                    （全称）    （公章）    负责人：                  （公章）                      年     月     日\n"));
    }
}
