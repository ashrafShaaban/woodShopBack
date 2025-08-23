package tmd.tmdAdmin.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tmd.tmdAdmin.data.repositories.ContactRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ContactService {
    private final ContactRepository contactRepository;

    public List<Integer> getMonthlyCounts() {
        List<Integer> counts = new ArrayList<>();
//        for (int month = 1; month <= 12; month++) {
//            counts.add(contactRepository.countByMonth(month));
//        }
        return counts;
    }
}
