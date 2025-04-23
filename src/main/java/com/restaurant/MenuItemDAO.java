package com.restaurant;

import org.hibernate.Session;
import java.util.List;

public class MenuItemDAO {
    public List<MenuItem> getAllMenuItems() {
        try (Session session = KoneksiDB.getSessionFactory().openSession()) {
            return session.createQuery("FROM MenuItem WHERE is_available = true", MenuItem.class).list();
        }
    }

    public MenuItem getMenuItemById(int id) {
        try (Session session = KoneksiDB.getSessionFactory().openSession()) {
            return session.get(MenuItem.class, id);
        }
    }
}