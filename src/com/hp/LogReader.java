package com.hp;

import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

/**
 * Created by jackl on 2016/8/29.
 */
public class LogReader extends Thread {
    private JTextArea msg;
    Session sess;
    String cmd;
    String imei;
    String vin;
    String imeiHex;
    int watchType;

    public LogReader(Session sess, String cmd, JTextArea msg, int watchType, String imei, String vin) {
        this.sess = sess;
        this.cmd = cmd;
        this.msg = msg;
        this.watchType = watchType;
        this.imei = imei;
        this.vin = vin;
    }

    @Override
    public void run() {
        System.out.println((new Date().toLocaleString() + " 数据接收开始>>>>>>>>>>\n" + cmd));
        read();
    }

    public void read() {
        try {
            if (imei != null) {
                imeiHex = parseByte2HexStr(imei.getBytes());
                System.out.println("imeiHex:" + imeiHex);
            }
            sess.execCommand(cmd);
            System.out.println("Here is some information from remote server:");
            InputStream stdout = new StreamGobbler(sess.getStdout());
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout, "UTF-8"));
            while (true) {
                String line = br.readLine();
                String filerStr = filter(line);
                if (filerStr != null) {
                    msg.append(filerStr + "\n");
                    // System.out.println("LINE:" + filerStr);
                }

            }
        } catch (IOException ee) {
            ee.printStackTrace(System.err);
        }
    }

    public String parseByte2HexStr(byte buf[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase() + " ");
        }
        return sb.toString();
    }


    /**
     * 逐行过滤
     *
     * @param str 文本
     * @return
     */
    public String filter(String str) {
        // replace [0x13][0x14][0x24][0x25][0x28][0x29][0x31]
        boolean check = false;
        if (str != null) {
            System.out.println(str);
            if (str.indexOf(imeiHex) > -1) {//imei匹配
                check = true;
            }
            if (vin.length() == 17 && str.indexOf(vin) > -1) {//vin匹配
                check = true;
            }
            if (str.indexOf("exception") > -1) {//exception输出
                check = true;
            }
            if (watchType == 0) {//全部
                check = true;
            }
            if (watchType == 1) {//远程控制
                if (str.indexOf("[0x31]") > -1 || str.indexOf("[0x13]") > -1 || str.indexOf("[0x14]") > -1) {
                    check = true;
                }
                if (watchType == 2) {//实时数据
                    if (str.indexOf("[0x22]") > -1 || str.indexOf("[0x23]") > -1)
                        check = true;
                }
                if (watchType == 3) {//故障数据
                    if (str.indexOf("[0x28]") > -1 || str.indexOf("[0x29]") > -1)
                        check = true;
                }
                if (watchType == 4) {//报警数据
                    if (str.indexOf("[0x24]") > -1 || str.indexOf("[0x25]") > -1)
                        check = true;
                }
                if (watchType == 5) {//注册数据
                    if (str.indexOf("[0x13]") > -1 || str.indexOf("[0x14]") > -1 || str.indexOf("Socket") > -1 || str.indexOf("exceptionCaught") > -1)
                        check = true;
                }
            }
            if (check) {
               // str = str.replaceAll("AlarmService", "监控服务");
                return str;
            } else {
                return null;
            }

        }
        return null;
    }
}