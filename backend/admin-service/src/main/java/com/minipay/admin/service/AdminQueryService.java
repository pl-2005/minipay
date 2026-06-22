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

    public List<AdminOrderResponse> listOrders(String keyword, String status) {
        return adminRepository.listOrders(keyword, status);
    }

    public List<AdminPaymentResponse> listPayments(String keyword, String status) {
        return adminRepository.listPayments(keyword, status);
    }

    public List<AdminEventResponse> listEvents(String keyword, String eventType, String status) {
        return adminRepository.listEvents(keyword, eventType, status);
    }

    public List<AdminNotificationResponse> listNotifications(String keyword, String status) {
        return adminRepository.listNotifications(keyword, status);
    }
}
