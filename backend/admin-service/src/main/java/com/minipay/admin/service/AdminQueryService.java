package com.minipay.admin.service;

import java.util.List;

import com.minipay.admin.repository.AdminRepository;

import org.springframework.stereotype.Service;

@Service
public class AdminQueryService {
    private final AdminRepository adminRepository;

    public AdminQueryService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public List<AdminOrderResponse> listOrders(String status) {
        return adminRepository.listOrders(status);
    }

    public List<AdminPaymentResponse> listPayments() {
        return adminRepository.listPayments();
    }

    public List<AdminEventResponse> listEvents() {
        return adminRepository.listEvents();
    }

    public List<AdminNotificationResponse> listNotifications() {
        return adminRepository.listNotifications();
    }
}
