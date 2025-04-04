package com.example.expensetracker.controller;

import com.example.expensetracker.model.Expense;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Controller
public class ExpenseController {
    private static final String URL = "jdbc:mysql://localhost:3306/expensetracker";
    private static final String USER = "root";
    private static final String PASSWORD = "anand9791";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    @GetMapping("/")
    public String viewExpenses(Model model) {
        List<Expense> expenses = new ArrayList<>();
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM expenses")) {
            while (rs.next()) {
                expenses.add(new Expense(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("amount"),
                        rs.getString("category"),
                        rs.getString("date")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Failed to load expenses: " + e.getMessage());
        }
        model.addAttribute("expenses", expenses);
        return "view_expenses";
    }

    @GetMapping("/add")
    public String addExpenseForm(Model model) {
        model.addAttribute("expense", new Expense());
        return "add_expense";
    }

    @PostMapping("/add")
    public String addExpense(@ModelAttribute Expense expense, RedirectAttributes redirectAttributes) {
        String query = "INSERT INTO expenses (name, amount, category, date) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, expense.getName());
            stmt.setDouble(2, expense.getAmount());
            stmt.setString(3, expense.getCategory());
            stmt.setString(4, expense.getDate());
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                redirectAttributes.addFlashAttribute("successMessage", "Expense added successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to add expense: No rows affected.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to add expense: " + e.getMessage());
        }
        return "redirect:/";
    }

    @GetMapping("/delete/{id}")
    public String deleteExpense(@PathVariable int id, RedirectAttributes redirectAttributes) {
        String query = "DELETE FROM expenses WHERE id = ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                redirectAttributes.addFlashAttribute("successMessage", "Expense deleted successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Expense with ID " + id + " not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete expense: " + e.getMessage());
        }
        return "redirect:/";
    }

    @GetMapping("/update/{id}")
    public String updateExpenseForm(@PathVariable int id, Model model) {
        Expense expense = null;
        String query = "SELECT * FROM expenses WHERE id = ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                expense = new Expense(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("amount"),
                        rs.getString("category"),
                        rs.getString("date"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        model.addAttribute("expense", expense);
        return "update_expense";
    }

    @PostMapping("/update")
    public String updateExpense(@ModelAttribute Expense expense, RedirectAttributes redirectAttributes) {
        String query = "UPDATE expenses SET name=?, amount=?, category=?, date=? WHERE id=?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, expense.getName());
            stmt.setDouble(2, expense.getAmount());
            stmt.setString(3, expense.getCategory());
            stmt.setString(4, expense.getDate());
            stmt.setInt(5, expense.getId());
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                redirectAttributes.addFlashAttribute("successMessage", "Expense updated successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Expense with ID " + expense.getId() + " not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update expense: " + e.getMessage());
        }
        return "redirect:/";
    }

    @GetMapping("/search")
    public String searchExpenses(@RequestParam String keyword, Model model) {
        List<Expense> expenses = new ArrayList<>();
        String query = "SELECT * FROM expenses WHERE name LIKE ? OR category LIKE ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, "%" + keyword + "%");
            stmt.setString(2, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                expenses.add(new Expense(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("amount"),
                        rs.getString("category"),
                        rs.getString("date")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Failed to search expenses: " + e.getMessage());
        }
        if (expenses.isEmpty()) {
            model.addAttribute("message", "No expenses found for keyword: " + keyword);
        }
        model.addAttribute("expenses", expenses);
        return "view_expenses";
    }
}