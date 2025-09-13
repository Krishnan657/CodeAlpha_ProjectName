import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 * StudentGradeTracker.java
 * Console-based student grade manager.
 *
 * Features:
 * - Add student
 * - Add grades for a student
 * - Calculate average, highest, lowest per student
 * - Display summary report for all students
 * - Remove student / view student details
 *
 * Compile: javac StudentGradeTracker.java
 * Run:     java StudentGradeTracker
 */
public class StudentGradeTracker {
    static class Student {
        String name;
        List<Double> grades = new ArrayList<>();

        Student(String name) {
            this.name = name;
        }

        void addGrade(double g) {
            grades.add(g);
        }

        double average() {
            if (grades.isEmpty()) return 0.0;
            double sum = 0.0;
            for (double g : grades) sum += g;
            return sum / grades.size();
        }

        double highest() {
            if (grades.isEmpty()) return 0.0;
            return Collections.max(grades);
        }

        double lowest() {
            if (grades.isEmpty()) return 0.0;
            return Collections.min(grades);
        }

        String gradeSummary() {
            if (grades.isEmpty()) return "No grades.";
            return String.format("Avg: %.2f, High: %.2f, Low: %.2f, Count: %d",
                    average(), highest(), lowest(), grades.size());
        }
    }

    private final List<Student> students = new ArrayList<>();
    private final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        StudentGradeTracker app = new StudentGradeTracker();
        app.run();
    }

    private void run() {
        System.out.println("=== Student Grade Tracker ===");
        boolean running = true;
        while (running) {
            printMenu();
            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1" -> addStudent();
                case "2" -> addGradeToStudent();
                case "3" -> viewStudentDetails();
                case "4" -> removeStudent();
                case "5" -> displaySummaryReport();
                case "6" -> running = false;
                default -> System.out.println("Invalid option. Try again.");
            }
        }
        System.out.println("Goodbye.");
    }

    private void printMenu() {
        System.out.println("\nMenu:");
        System.out.println("1. Add new student");
        System.out.println("2. Add grade to student");
        System.out.println("3. View student details");
        System.out.println("4. Remove student");
        System.out.println("5. Display summary report (all students)");
        System.out.println("6. Exit");
        System.out.print("Choose: ");
    }

    private void addStudent() {
        System.out.print("Enter student name: ");
        String name = sc.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Name empty. Cancelled.");
            return;
        }
        students.add(new Student(name));
        System.out.println("Added student: " + name);
    }

    private Student findStudentByName(String name) {
        for (Student s : students) if (s.name.equalsIgnoreCase(name)) return s;
        return null;
    }

    private void addGradeToStudent() {
        System.out.print("Student name: ");
        String name = sc.nextLine().trim();
        Student s = findStudentByName(name);
        if (s == null) {
            System.out.println("Student not found. Create new? (y/n): ");
            String ans = sc.nextLine().trim();
            if (ans.equalsIgnoreCase("y")) {
                s = new Student(name);
                students.add(s);
                System.out.println("Created student: " + name);
            } else {
                return;
            }
        }
        System.out.print("Enter grade (number): ");
        try {
            double g = Double.parseDouble(sc.nextLine().trim());
            if (g < 0) {
                System.out.println("Grade must be non-negative.");
                return;
            }
            s.addGrade(g);
            System.out.println("Added grade " + g + " to " + s.name);
        } catch (NumberFormatException e) {
            System.out.println("Invalid number.");
        }
    }

    private void viewStudentDetails() {
        System.out.print("Student name: ");
        String name = sc.nextLine().trim();
        Student s = findStudentByName(name);
        if (s == null) {
            System.out.println("Student not found.");
            return;
        }
        System.out.println("Name: " + s.name);
        if (s.grades.isEmpty()) {
            System.out.println("No grades yet.");
        } else {
            System.out.println("Grades: " + s.grades);
            System.out.println(s.gradeSummary());
        }
    }

    private void removeStudent() {
        System.out.print("Student name to remove: ");
        String name = sc.nextLine().trim();
        Student s = findStudentByName(name);
        if (s == null) {
            System.out.println("Student not found.");
            return;
        }
        students.remove(s);
        System.out.println("Removed " + name);
    }

    private void displaySummaryReport() {
        System.out.println("\n--- Summary Report ---");
        if (students.isEmpty()) {
            System.out.println("No students to report.");
            return;
        }
        int idx = 1;
        for (Student s : students) {
            System.out.printf("%d) %s -> %s%n", idx++, s.name, s.gradeSummary());
        }

        // class-level stats (optional)
        double classSum = 0.0;
        int gradeCount = 0;
        double classHigh = Double.MIN_VALUE;
        double classLow = Double.MAX_VALUE;
        for (Student s : students) {
            for (double g : s.grades) {
                classSum += g;
                gradeCount++;
                if (g > classHigh) classHigh = g;
                if (g < classLow) classLow = g;
            }
        }
        if (gradeCount > 0) {
            System.out.printf("Class average: %.2f, Class high: %.2f, Class low: %.2f, Total grades: %d%n",
                    classSum / gradeCount, classHigh, classLow, gradeCount);
        } else {
            System.out.println("No grades recorded for class-level stats.");
        }
    }
}
