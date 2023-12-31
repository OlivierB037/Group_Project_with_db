package fr.cnam.group;

import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;
import java.awt.event.*;

import java.sql.SQLException;

public class Main {
    public static SQLConnexion sqlConnect;

    public static void main(String[] args) throws Exception {
//        Main main = new Main();
//        try {
//            main.sqlConnexion = new SQLConnexion();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        main.sqlConnexion.connect(SQLConnexion.defaultURL ,"admin","password");

        //Administrateur administrateur = new Administrateur("Baylac","Olivier","1989-11-23",2);


        try {
            UIManager.setLookAndFeel(new NimbusLookAndFeel());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        MyWindow myWindow = new MyWindow();



        TopMenu topMenu = new TopMenu();
        topMenu.getReturnToMain().setVisible(false);
        MenuPrincipal menu1 = new MenuPrincipal();
        myWindow.setJMenuBar(topMenu);

        myWindow.setVisible(true);
        myWindow.pack();
        /*ouverture du menu principal*/


        myWindow.setContentPane(menu1.getMenuPrincipal());
        myWindow.setMinimumSize(new Dimension(600,600));

        sqlConnect = new SQLConnexion();

        //ouverture de la connexion à la bd


        //bouton connexion
        menu1.getConnecterButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sqlConnect.openConnect(myWindow);
                //menu1.getConnecterButton().setText("déconnexion");


            }

        });

        menu1.getConsulterButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (sqlConnect.getConnection() == null || sqlConnect.getCurrentUser() == null || !sqlConnect.getConnection().isValid(0)) {
                        JOptionPane.showMessageDialog(myWindow, "vous n'êtes pas connecté","non connecté",JOptionPane.ERROR_MESSAGE);

                    } else {
                        topMenu.getReturnToMain().setVisible(true);
                        topMenu.getReturnToMain().addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                myWindow.setContentPane(menu1.getMenuPrincipal());
                                topMenu.getReturnToMain().setVisible(false);
                            }
                        });
                        System.out.println("test 1");
                        MenuConsulter menuConsulter = new MenuConsulter(sqlConnect.getConnection());
                        System.out.println("test 2");
                        myWindow.setContentPane(menuConsulter.getConsultPane());
                    }
                }catch (SQLException err){
                    JOptionPane.showMessageDialog(myWindow,err.getMessage());
                }
            }
        });


        menu1.getAjouterButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    if (sqlConnect.getConnection() != null && sqlConnect.getConnection().isValid(0)) { // ajouter condition administrateur
                        if (sqlConnect.getCurrentUser() instanceof Administrateur || sqlConnect.getCurrentUser() instanceof RootUser) {
                            topMenu.getReturnToMain().setVisible(true);
                            topMenu.getReturnToMain().addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    myWindow.setContentPane(menu1.getMenuPrincipal());
                                    topMenu.getReturnToMain().setVisible(false);
                                }
                            });

                           /*ouverture du menu ajouter un utilisateur*/
                            AjouterUser ajouterUser = new AjouterUser();
                            myWindow.setContentPane(ajouterUser.getPanelAjouterUser());


                        }
                        else{
                            JOptionPane.showMessageDialog(myWindow,"only administrateurs can access this section","Accès Refusé",JOptionPane.ERROR_MESSAGE);
                        }




                    } else {
                        JOptionPane.showMessageDialog(null, "vous n'êtes pas connecté à une base de données");
                    }
                }catch (SQLException err){
                    JOptionPane.showMessageDialog(null,err.toString());
                }
            }
        });//fin du listener menu ajouter

        myWindow.addWindowStateListener(e -> {
            if (e.getNewState() == WindowEvent.WINDOW_CLOSING){
                System.out.println("window closing");

            }
            else if(e.getNewState() == WindowEvent.WINDOW_CLOSED){
                System.out.println("window closed");
            }
            else{
                System.out.println("window event : " + e.getNewState());
            }
        });


        myWindow.addWindowListener(sqlConnect);


        menu1.getQuitterButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(sqlConnect.getConnection() == null){
                    System.exit(0);
                }
                else {
                    sqlConnect.EndConnection();
                    System.exit(0);
                }
            }
        });
        topMenu.getQuit().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(sqlConnect.getConnection() == null){
                    System.exit(0);
                }
                else {
                    sqlConnect.EndConnection();
                    System.exit(0);
                }
            }
        });


    }
}
