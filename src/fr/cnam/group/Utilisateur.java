package fr.cnam.group;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class Utilisateur {





    static String registerUserAccessQuery = "CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(\n" +
            "    'derby.user.%s', '%s')"; //1st variable : user name; 2nd variable : password
    static String removeQuery =  "CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(\n" +
            "    'derby.user.%s', null)";// variable : user name
    private String identifiant_user;
    public enum User_statut {Administrateur, Particulier, RootUser, Invite}
    private String nom_user;
    private String prenom_user;
    private String date_user;
    private User_statut user_statut;
    private int ref_user;
    private static String setStatutQuery;

    public Utilisateur(String identifiant, String nom, String prenom, String date, int ref) throws Exception {
        this.identifiant_user = identifiant;
        this.nom_user = nom;
        this.prenom_user = prenom;
        this.date_user = date;
        if (!isDateFormatOk(date_user)){
            throw new Exception("wrong date format; required format : YYYY-MM-DD");
        }
        this.ref_user = ref;
        if(ref_user == 0){
            this.user_statut = User_statut.RootUser;
        }
        else{
            this.user_statut = checkUserStatut();
            if (user_statut == null){
                throw new Exception("user Status not found");
            }
        }


    }



    public User_statut checkUserStatut() throws SQLException {
        System.out.println("new Utilisateur: checking statut");
        String checkQuery = String.format("SELECT * FROM users \n" +
                "WHERE ref_user = %d\n", ref_user);

        try {
            Statement statement = Main.sqlConnect.getConnection().createStatement();
            System.out.println("checkUserStatut() : query =" + checkQuery);
            ResultSet resultSet = statement.executeQuery(checkQuery);
            if(resultSet.next() && resultSet.getString("nom_user").equals(nom_user)) {
                return User_statut.valueOf(resultSet.getString("statut_user"));

            }

        } catch (SQLException e) {
            throw e;
        }
        return null;


    }
    public static Utilisateur createUserFromDataBase(String identifiant) throws Exception {

        Utilisateur utilisateur = null;



        String checkQuery = String.format("SELECT * FROM users \n" +
                "WHERE identifiant_user = '%s'", identifiant);

        int ref;
        String nom;
        String prenom;
        String date;
        User_statut statut;

        Statement statement = Main.sqlConnect.getConnection().createStatement();
        System.out.println("createUserFromDatabase() : query = " + checkQuery);
        ResultSet userResult = statement.executeQuery(checkQuery);

        if (userResult.next()) {
            System.out.println("user found in database");
            ref = userResult.getInt("ref_user");
            nom = userResult.getString("nom_user");
            prenom = userResult.getString("prenom_user");
            date = userResult.getString("date_user");
            System.out.println("date : " + date);
            statut = User_statut.valueOf(userResult.getString("statut_user"));

        } else {
            userResult.close();
            throw new SQLException(" user not in database");
        }
                        /*
                        création de l'utilisateur courant
                         */

        if (statut == User_statut.Administrateur) {
            System.out.println("user is an administrateur");
            utilisateur = new Administrateur(identifiant, nom, prenom, date, ref);
        } else if (statut == User_statut.Particulier) {
            System.out.println("user is a particulier");
            utilisateur = new Particulier(identifiant,nom, prenom, date, ref);
        }
        userResult.close();
        return utilisateur;

    }



    public static boolean isDateFormatOk(String date)  {
        System.out.println("date checked : " + date);
        if (date.matches("^(0[1-9]|1[012])[- /.](0[1-9]|[12][0-9]|3[01])[- /.](19|20)\\d\\d$") ||
                 date.matches("^(19|20)\\d\\d[- /.](0[1-9]|1[012])[- /.](0[1-9]|[12][0-9]|3[01])$")){
            return true;
        }
        else return false;
    }

    public static String formatNames(String name){
        name = name.replaceFirst("[a-zA-Z]", String.valueOf(Character.toUpperCase(name.charAt(0))));
        return name.split("\\s")[0];

    }
    public void changeUserStatut(Utilisateur utilisateur,User_statut newStatut) throws Exception {
        Utilisateur currentUser = Main.sqlConnect.getCurrentUser();

        if (utilisateur.user_statut.equals(User_statut.RootUser)){
            throw new Exception("can't modify Root Users");
        }
        if (currentUser.user_statut.equals(User_statut.Administrateur)  && !utilisateur.user_statut.equals(User_statut.Particulier)){//pas d'administrateur voulant modifier un non particulier
            throw new Exception("Administrateur can only change particulier's statut");
        }
        else if(currentUser.user_statut.equals(User_statut.Particulier)){ //pas de particulier voulant modifier
            throw new Exception("restricted operations - you need to be administrateur to perform this operation");
        }
        else {
            if (utilisateur.user_statut.equals(newStatut)){ // pas de statut déja défini
                System.err.println("user status already set to " + newStatut);
                throw new Exception("user status already set to " + newStatut);
            }
            if (utilisateur.user_statut.equals(User_statut.Administrateur) && currentUser.user_statut.equals(User_statut.RootUser) || utilisateur.user_statut.equals(User_statut.Particulier)){
                String AccessRightsQuery = String.format("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(\n" +
                        "    'derby.database.%s', '%s')", (newStatut.equals(User_statut.Administrateur)) ? "fullAccessUsers" : "readOnlyAccessUsers", utilisateur.identifiant_user);

                Main.sqlConnect.sendUpdate(AccessRightsQuery);
                String dataQuery = String.format("UPDATE USERS\n" +
                        "SET statut_user = '%s' \n" +
                        "WHERE ref_user = %d", newStatut.toString(), utilisateur.ref_user);
                Main.sqlConnect.sendUpdate(dataQuery);

            }

        }
    }

    public static int checkId(String nom, String prenom,String date) throws SQLException {



        String selectQuery = String.format("SELECT ref_user FROM users \n"+
                "WHERE nom_user = '%s'\n"+
                "AND PRENOM_USER = '%s'\n" +
                "AND DATE_USER = '%s'", nom, prenom, date);
        int userId;
        ResultSet slct;



        System.out.println("checkId() : query = " +selectQuery);
        try (Statement statement = Main.sqlConnect.getConnection().createStatement()) {
            slct = statement.executeQuery(selectQuery);
            if (slct.next()) {
                userId = slct.getInt("ref_user");
                System.out.println("id trouvée" + userId);
            } else {
                System.out.println("pas de next");
                userId = -1;
            }
        }
        return userId;
    }



    public boolean remove() throws Exception {
        String query = String.format(removeQuery, identifiant_user);
        int affectedRows = Main.sqlConnect.sendUpdate(query);
        if (affectedRows == 0){
            throw new Exception("erreur suppression invité");
        }
        else return true;
    }

    public String getIdentifiant_user() {
        return identifiant_user;
    }

    public int getRef_user() {
        return ref_user;
    }

    public String getNom_user() {
        return nom_user;
    }

    public void setNom_user(String nom_user) {
        this.nom_user = nom_user;
    }

    public String getPrenom_user() {
        return prenom_user;
    }

    public void setPrenom_user(String prenom_user) {
        this.prenom_user = prenom_user;
    }

    public String getDate_user() {
        return date_user;
    }

    public void setDate_user(String date_user) {
        this.date_user = date_user;
    }
}
