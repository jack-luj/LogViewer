package com.hp;


import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;


/**
 * Created by jackl on 2016/10/17.
 */
public class LogViewer  extends JFrame {
    String hostname = "";
    String username = "";
    String password = "";
    int maxLines=50;
    String filepath="";///home/jackl/Desktop/ping.txt

    String[] types = {"全部", "远程控制","实时数据" ,"故障数据" ,"报警数据","注册/唤醒" };
    JButton bt1= new JButton("连接");
    JButton bt2= new JButton("断开");

    JButton bt3= new JButton("查看");
    JButton bt4= new JButton("停止");

    JLabel  lab1=new JLabel("host:");
    JTextField host=new JTextField("");//192.168.137.130
    JLabel  lab2=new JLabel("username:");
    JTextField t_username=new JTextField("");
    JLabel  lab3=new JLabel("password:");
    JPasswordField t_password=new JPasswordField("");

    JLabel  lab8=new JLabel("文件路径:");
    JTextField t_filename=new JTextField("/home/vat/BriAir/BriAir.log");


    JLabel  lab4=new JLabel("imei:");
    JTextField t_imei=new JTextField("");
    JLabel  lab5=new JLabel("vin:");
    JTextField t_vin=new JTextField("");
    JLabel  lab6=new JLabel("起始时间:");
    JTextField t_sdate=new JTextField("");
    JLabel  lab7=new JLabel("关注业务:");
    JComboBox t1 = new JComboBox(types);
    JScrollPane jScrollPane;
    Connection conn=null;
    Session sess=null;
    Container panel;
    LogReader logReader;
    JTextArea textArea=new JTextArea("文本区双击左键调整字体\n文本区双击右键清空日志\n");
    int fontSize=14;
    Font font = new Font("", Font.ROMAN_BASELINE, fontSize);
    public LogViewer()
    {
        this.setTitle("LogViewer - v1.0");
         panel = getContentPane();

        panel.setLayout(null);
        lab1.setBounds(10, 20, 30, 20);   panel.add(lab1);
        host.setBounds(40, 20, 120, 20);  panel.add(host);
        lab2.setBounds(180, 20, 80, 20);   panel.add(lab2);
        t_username.setBounds(260, 20, 90, 20);  panel.add(t_username);
        lab3.setBounds(370, 20, 70, 20);   panel.add(lab3);
        t_password.setBounds(440, 20, 120, 20);  panel.add(t_password);
        t_password.setEchoChar('●');
        lab8.setBounds(580, 20, 70, 20);   panel.add(lab8);
        t_filename.setBounds(650, 20, 150, 20);  panel.add(t_filename);
        bt1.setBounds(810, 20, 60, 20);   panel.add(bt1);
        bt2.setBounds(880, 20, 60, 20);   panel.add(bt2);

        lab4.setBounds(10, 40, 30, 20);   panel.add(lab4);
        t_imei.setBounds(40, 40, 120, 20);  panel.add(t_imei);
        //lab5.setBounds(180, 40, 20, 20);   panel.add(lab5);
        //t_vin.setBounds(200, 40, 150, 20);  panel.add(t_vin);
        //lab6.setBounds(370, 40, 70, 20);   panel.add(lab6);
        //t_sdate.setBounds(440, 40, 120, 20);  panel.add(t_sdate);
        t_sdate.setEnabled(false);
        lab7.setBounds(580, 40, 70, 20);   panel.add(lab7);
        t1.setBounds(650, 40, 150, 20);  panel.add(t1);
        bt3.setBounds(810, 40, 60, 20);   panel.add(bt3);
        bt4.setBounds(880, 40, 60, 20);   panel.add(bt4);

        jScrollPane=new JScrollPane(textArea);
        jScrollPane.setBounds(20, 80, 920, 450);    panel.add(jScrollPane);
        textArea.setAutoscrolls(true);
        textArea.setFont(font);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);            // 激活断行不断字功能
        textArea.setFont(font);

        bt2.setEnabled(false);
        bt3.setEnabled(false);
        bt4.setEnabled(false);

        bt1.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        hostname = host.getText();
                        username = t_username.getText();
                        password = new String(t_password.getPassword());
                        try {
                            conn = new Connection(hostname);
                            conn.connect();
                            boolean isAuthenticated = conn.authenticateWithPassword(username, password);
                            if (isAuthenticated == false){
                                JOptionPane.showMessageDialog(null,"用户名或密码不正确!","SSH连接失败",JOptionPane.WARNING_MESSAGE);
                                return;
                            }
                            System.out.println("连接成功");
                            bt1.setEnabled(false);
                            bt2.setEnabled(true);
                            bt3.setEnabled(true);
                            bt4.setEnabled(false);
                        } catch (IOException ee) {
                            ee.printStackTrace(System.err);
                        }

                    }
                });

        bt2.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        //jEditorPane.setContentType("text/html");
                        if (Desktop.isDesktopSupported()) {
                            try {
                                System.out.println("断开连接");
                                bt1.setEnabled(true);
                                bt2.setEnabled(false);
                                bt3.setEnabled(false);
                                bt4.setEnabled(false);
                                if(sess!=null) {
                                    sess.close();
                                }
                                if(conn!=null) {
                                    conn.close();
                                }
                            } catch (Exception ee) {
                                ee.printStackTrace();
                            }
                        }
                    }
                });

        bt3.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        System.out.println("查看");
                        textArea.setText("");
                        bt3.setEnabled(false);
                        bt4.setEnabled(true);
                        filepath=t_filename.getText();
                        try {
                            sess = conn.openSession();
                            String cmd="tail -n " + maxLines + " -f " + filepath + "";
                            int watchType=t1.getSelectedIndex();
                            String imei=t_imei.getText();
                            String vin=t_vin.getText();
                            logReader=new LogReader(sess,cmd,textArea,watchType,imei,vin);
                            logReader.start();
                        } catch (IOException ee) {
                            ee.printStackTrace(System.err);
                        }
                    }
                });

        bt4.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        //jEditorPane.setContentType("text/html");
                            try {
                                System.out.println("停止");
                                bt3.setEnabled(true);
                                bt4.setEnabled(false);
                                logReader.interrupt();
                                sess.close();
                                logReader=null;
                            } catch (Exception ee) {
                                ee.printStackTrace();
                            }
                    }
                });


        this.addWindowStateListener(new WindowStateListener() {//最大化时重新初始化数组
            public void windowStateChanged(WindowEvent e) {
                System.out.println("state changed");
                int width=e.getWindow().getWidth();
                int height=e.getWindow().getHeight();
                jScrollPane.setBounds(20, 80, width-80, height-150);
                panel.repaint();
            }
        });

        this.setSize(1000, 600);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setVisible(true);

        textArea.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
                int z = e.getButton();
                int count = e.getClickCount();
                if (z == 1&&count == 2) {//左键双击 字号+
                    if(fontSize<18){
                        fontSize++;
                    }else{
                        fontSize=10;
                    }
                    Font font = new Font("", Font.ROMAN_BASELINE, fontSize);
                    textArea.setFont(font);

                }
                if (z == 3&&count == 2) {//右键双击 清空文本
                    textArea.setText("");
                }
                panel.repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });
    }
    public static void main(String[]args)
    {
        new LogViewer();
    }




}
