package de.dragonrex.test;

import de.dragonrex.Con4J;

import java.util.Scanner;

public class Client2 {
    public static Con4J con4J;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        con4J = new Con4J(".con4J", "Client-2");
        con4J.onReceive((msg) -> {
            System.out.println("[CON4J] -> Message Incoming:");
            System.out.println(msg.id());
            System.out.println(msg.data());
            System.out.println(msg.channel());
        });
        System.out.println("Starting Client 2");
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if(line.startsWith("send")) {
                String[] split = line.split(":");
                con4J.writeFile(split[1], split[2]);
                System.out.println("[CON4J] -> Message Send!");
            }
            if(line.startsWith("addSub")) {
                String[] split = line.split(":");
                con4J.addSubChannel(split[1]);
                System.out.println("[CON4J] -> SubChannel " + split[1] + " created!");
            }
        }
    }
}
