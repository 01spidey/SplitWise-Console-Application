package com.company;

import java.time.LocalDate;
import java.util.*;

class User {
    String name;
    String mail;
    HashMap<String, Group> group_map = new HashMap<>();
    double settle_amt, receive_amt;

    User(String name, String mail) {
        this.name = name;
        this.mail = mail;
    }

    void createGroup(String groupName, String desc, String groupID, ArrayList<User> members) {

        if (!group_map.containsKey(groupID)) {
            Group group = new Group(groupName, desc, groupID);
            members.add(this);
            for (User member : members) group.addMember(member, false);
            System.out.println("\nGroup Created Successfully !!");
            group_map.put(groupID, group);
            SplitWise.group_map.put(groupID, group);
        } else System.out.println("\nGroup Already Exists!!");
    }

    void joinGroup(Group group, String groupID) {
        if (group_map.containsKey(groupID)) System.out.println("You are already added in the group !!");
        else {
            group.addMember(this, false);
            System.out.println("Joined the Group \"" + group.groupName + "\" Successfully !!");
        }
    }

    void myGroups() {
        System.out.println("\n---------- My Groups ----------");
        ArrayList<String> groups = new ArrayList<>(group_map.keySet());

        if (groups.size() > 0) {
            for (int i = 0; i < groups.size(); i++)
                System.out.println((i + 1) + ". " + group_map.get(groups.get(i)).groupName);
            System.out.println((groups.size() + 1) + ". Go Back");
            System.out.print("\nChoose a Group : ");
            Scanner scn = new Scanner(System.in);
            int op = scn.nextInt();
            if (op <= groups.size()) group_map.get(groups.get(op - 1)).viewGroup(this);

        } else System.out.println("\nNo Groups !!");
    }

}

class Expense {
    String expenseName;
    User paidBy;
    double amount;
    Group group;
    LocalDate date;
    HashMap<String, Double> share_map = new HashMap<>();
    ArrayList<String> members;


    Expense(String name, double amount, User paidBy, Group group, List<String> share_lst){
        expenseName = name;
        this.amount = amount;
        this.paidBy = paidBy;
        this.group = group;
        date = LocalDate.now();
        share_expense(share_lst);
    }

    void share_expense(List<String> share_lst){
        double share_amt = amount/(share_lst.size()+1);
        members = new ArrayList<>(group.member_map.keySet());

        for(String memberID:members){
            if(!(memberID.equals(paidBy.name))) {
                if(share_lst.contains(memberID)) {
                    share_map.put(memberID, -share_amt);
                    group.member_map.get(memberID).settle_amt += share_amt;
                }else share_map.put(memberID, 0.0);
            }
            else {
                share_map.put(memberID, amount-share_amt);
                group.member_map.get(memberID).receive_amt+=(amount - share_amt);
            }
        }
    }

    void settleAmount(User user){
        String memberID = user.name;
        String paidId = paidBy.name;
        double share_amt = share_map.get(memberID);

        if(share_amt<0) {
            share_map.put(paidId, share_map.get(paidId) + share_amt);
            share_map.put(memberID, 0.0);
            group.member_map.get(memberID).settle_amt += share_amt;
            group.member_map.get(paidId).receive_amt += share_amt;
            System.out.printf("\nSettled Rs.%.2f to %s !!",(-share_amt),paidBy.name);
        }else System.out.println("Nothing to Settle !!");
    }

    void displayDetails(User user){
        System.out.println("\n---------- Expense ----------");
        System.out.println("Expense Name : "+expenseName+"\nAmount : Rs."+amount+"\nPaid By : "+paidBy.name+"\nDate : "+(date.toString()));
        System.out.println("\nExpense Share Details :");
        User member;
        members = new ArrayList<>(group.member_map.keySet());
        for(String memberID:members){
            member = group.member_map.get(memberID);
            System.out.printf("%s : Rs. %.2f\n",member.name,share_map.get(memberID));
        }

        System.out.println("\n1. Settle\n2. Remove Expense\n3. Go Back\n\nChoose an Option : ");
        int op = new Scanner(System.in).nextInt();
        if(op==1) settleAmount(user);
        else if(op==2) group.removeExpense(expenseName,user, paidBy);
    }
}

class Group {
    HashMap<String, User> member_map = new HashMap<>();
    HashMap<String, Expense> expense_map = new HashMap<>();
    String groupName;
    String groupDesc;
    String groupID;
    Scanner scn = new Scanner(System.in);

    Group(String name, String desc, String groupID) {
        groupName = name;
        groupDesc = desc;
        this.groupID = groupID;
    }

    void addMember(User user, boolean flag) {
        String key = user.name;
        if (member_map.containsKey(key)) System.out.println("\nUser already exists in the group !!");
        else {
            user.group_map.put(groupID, this);
            member_map.put(user.name, user);
            if (flag) System.out.println(user.name + " added Successfully!!");
        }
    }

    boolean viewMembers() {
        System.out.println("\n---------- Group Members ----------");
        ArrayList<String> members = new ArrayList<>(member_map.keySet());
        for (int i = 0; i < members.size(); i++) System.out.println((i + 1) + ". " + (member_map.get(members.get(i))).name);
        return false;
    }

    void viewGroup(User user) {
        System.out.println("\n---------- " + groupName + " ----------\nGroup ID : "+(groupID.split("/")[1])+"\n\nDescription : \n\t" + groupDesc);
        while (true) {
            System.out.print("\n1. Add Expense\n2. View Expenses\n3. View Members\n4. Add Member\n5. Leave Group\n6. Go Back\n\nChoose an option : ");
            int op = scn.nextInt();
            if (op == 1) {
                List<String> share_lst = new ArrayList<>();
                scn.nextLine();
                System.out.println("\n---------- Add Expense ----------");
                System.out.print("Expense Name : ");
                String expenseName = scn.nextLine();
                System.out.print("Expense Amount : ");
                double amount = scn.nextDouble();
                boolean flag = true;
                viewMembers();
                String ch = "";
                String username = "";
                while(flag){
                    System.out.print("\nAdd a member for share [y/n] : ");
                    ch = scn.next();
                    scn.nextLine();
                    if(ch.equals("y")){
                        System.out.print("Enter Username : ");
                        username = scn.nextLine();
                        if(member_map.containsKey(username)) share_lst.add(username);
                        else System.out.println("\nUser not found in the Group !!");
                    }else flag = false;
                }
                addExpense(expenseName, amount, user, share_lst);

            }
            else if(op==2){
                boolean flag = true;
                while(flag) {
                    System.out.println("\n---------- Group Expenses ----------");
                    List<String> expenses = new ArrayList<>(expense_map.keySet());
                    if(expenses.size()==0){
                        System.out.println("\nNo Expenses !!");
                        flag = false;
                    }else {
                        for (int i = 0; i < expenses.size(); i++) System.out.println((i + 1) + ". " + expenses.get(i));
                        System.out.println((expenses.size() + 1) + ". Go Back\n\n Choose an Option : ");
                        op = scn.nextInt();
                        if (op <= expenses.size()) {
                            expense_map.get(expenses.get(op - 1)).displayDetails(user);
                        } else flag = false;
                    }
                }
            }
            else if (op == 3) {
                boolean flag = true;
                while (flag) {
                    flag = viewMembers();
                }
            }
            else if(op==4){
                System.out.println("\n---------- Add Member ----------");
                scn.nextLine();
                System.out.print("Name : ");
                String name = scn.nextLine();
                if (SplitWise.user_map.containsKey(name)){
                    User member = SplitWise.user_map.get(name);
                    if(member_map.containsKey(name)) System.out.println("\nUser already added in the Group !!");
                    else {
                        member_map.put(name, member);
                        addMember(member,true);
                    }
                }
                else {
                    System.out.println("\nUser does not exist !!");
                }
            }
            else if (op == 5) {
                member_map.remove((user.name + "/" + user.mail));
                user.group_map.remove(groupID);
                if(member_map.size()==0) SplitWise.group_map.remove(groupID);
                System.out.println("\n You Left the Group !!");
                break;
            } else break;
        }
    }

    void addExpense(String expenseName, double amount, User paidBy, List<String> share_lst) {
        if (expense_map.containsKey(expenseName)) System.out.println("\nExpense Name already Exists !!");
        else {
            Expense expense = new Expense(expenseName, amount, paidBy, this, share_lst);
            expense_map.put(expense.expenseName, expense);
        }
    }

    public void removeExpense(String expenseName,User user, User paidBy) {
        if(user.equals(paidBy)){
            expense_map.remove(expenseName);
            System.out.printf("\n'%s' removed successfully !!\n",expenseName);
        }else System.out.printf("\nOnly %s can remove the Expense !!\n",paidBy.name);


    }
}

public class SplitWise {

    static Scanner scn = new Scanner(System.in);
    static HashMap<String, User> user_map = new HashMap<>();
    static HashMap<String, Group> group_map = new HashMap<>();

    static boolean validate(String name, String mail){
        return name.matches("^(?=.*[a-zA-Z])[a-zA-Z0-9]+$") &&
                mail.matches("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
    }

    static String userSignUp() {
        System.out.println("\n---------- User SignUp ----------");
        System.out.print("Name [at least 3 characters]: ");
        String name = scn.nextLine();
        System.out.print("Email : ");
        String mail = scn.next();
        if(validate(name, mail)) {
            if (user_map.containsKey(name)) {
                System.out.println("\nUsername Already Taken !!");
                return "null";
            }

            user_map.put(name, new User(name, mail));
            System.out.println("\nRegistration Success !!\nWelcome, " + name);
            return name;
        }else System.out.println("Username or Email Invalid !!");
        return "null";
    }

    static String askFirst() {
        System.out.println("\n----- SplitWise -----\n    1. Sign In\n    2. Sign Up\n    3. EXIT");
        System.out.print("\nChoose an option : ");
        int op = scn.nextInt();
        scn.nextLine();
        if (op == 1) {
            System.out.println("\n---------- User SignIn ----------");
            System.out.print("Name : ");
            String name = scn.nextLine();
            System.out.print("Email : ");
            String mail = scn.next();
            if (user_map.containsKey(name)) {
                System.out.println("\nWelcome, " + user_map.get(name).name + " !!");
                return name;
            } else {
                System.out.println("\nInvalid Credentials!!");
                return askFirst();
            }
        } else if (op == 2) return userSignUp();
        else return "exit";
    }

    public static void main(String[] args) {

        String cur_login = "";

        while (true) {
            cur_login = askFirst();

            if (cur_login.equals("exit")) {
                System.out.println("\nShutting the System Down!!");
                break;
            } else if (!cur_login.equals("null")) {
                boolean flag = true;
                while (flag) {
                    System.out.println("\n---------- User Portal ----------");
                    System.out.println("1. Create Group\n2. Join Group\n3. My Groups\n4. Logout");
                    System.out.print("\nChoose an option : ");
                    String op = scn.next();
                    User user = user_map.get(cur_login);

                    switch (op) {
                        case "1":
                            System.out.println("\n---------- Create Group ----------");
                            scn.nextLine();
                            System.out.print("Group Name : ");
                            String groupName = scn.nextLine();
                            System.out.print("Group Description : ");
                            String groupDesc = scn.nextLine();
                            System.out.print("Group ID : ");
                            String groupID = scn.nextLine();

                            groupID = groupName+"/"+groupID;

                            ArrayList<User> members = new ArrayList<>();
                            String name, mail;

                            int i = 0;
                            while (true) {
                                System.out.print("\nAdd a Member [y/n] : ");
                                String add = scn.next();
                                scn.nextLine();
                                if (add.equals("y")) {
                                    System.out.println("\nMember " + (i + 1));
                                    System.out.print("Name : ");
                                    name = scn.nextLine();
                                    if (user_map.containsKey(name))
                                        members.add(user_map.get(name));
                                    else {
                                        i--;
                                        System.out.println("User does not exist !!");
                                    }
                                } else break;

                                i++;
                            }

                            if(group_map.containsKey(groupID)) System.out.println("\nGroup ID not available !!");
                            else {
                                System.out.println("\nGroup ID : "+groupID);
                                user.createGroup(groupName, groupDesc, groupID, members);
                            }

                            break;

                        case "2":
                            System.out.println("---------- Join Group ----------");
                            scn.nextLine();
                            System.out.print("Group Name : ");
                            groupName = scn.nextLine();
                            System.out.print("Group ID : ");
                            groupID = scn.nextLine();
                            groupID = groupName+"/"+groupID;
                            if (group_map.containsKey(groupID)) {
                                user.joinGroup(group_map.get(groupID), groupID);
                            }
                            break;

                        case "3":
                            user.myGroups();
                            break;

                        case "4":
                            flag = false;
                            System.out.println("\nThank you for Visiting us !!");
                            break;
                    }
                }
            }
        }
    }
}
