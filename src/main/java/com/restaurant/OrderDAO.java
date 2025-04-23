package com.restaurant;

import org.hibernate.Session;
import org.hibernate.Transaction;

public class OrderDAO {
    public boolean saveOrder(Order order) {
        Session session = null;
        Transaction transaction = null;
        
        try {
            session = KoneksiDB.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            
            order.calculateTotal();
            session.save(order);
            
            for (OrderItem item : order.getItems()) {
                session.save(item);
            }
            
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}