package com.example.tenant.Services;

import com.example.tenant.Clients.AuthServiceClient;
import com.example.tenant.Clients.LicenceServiceClient;
import com.example.tenant.Dto.*;
import com.example.tenant.Entities.Tenant;
import com.example.tenant.Entities.UserSip;
import com.example.tenant.Repositories.TenantRepository;
import com.example.tenant.Repositories.UserSipRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j // Cette annotation crée automatiquement le logger
public class UserSipService {

    @Autowired
    private UserSipRepository userSipRepository;
    @Autowired
    private TenantRepository tenantRepository;


    @Autowired
    private AuthServiceClient authServiceClient;

    @Autowired
    private LicenceServiceClient licenceServiceClient;

    @Autowired
    private EmailService emailService;


    @Value("${licence.service.url}")
    private String licenceServiceUrl;

    @Value("${auth.service.url}")
    private String authServiceUrl;


    public void createUserSip(UserSipRequest request) throws Exception {
        Optional<Tenant> tenantOpt = tenantRepository.findById(request.getTenantId());

        if (tenantOpt.isEmpty()) {
            throw new Exception("Tenant non trouvé avec l'id: " + request.getTenantId());
        }

        Tenant tenant = tenantOpt.get();

        String domainName = request.getUsername() + "@symphonia.com";
        String contextName = request.getUsername() + "_context";
        String code = request.getUsername() + "-" + String.format("%03d", new Random().nextInt(1000));

        UserSip userSip = new UserSip();
        userSip.setTenant(tenant);
        userSip.setUsername(request.getUsername());
        userSip.setEmailSip(request.getEmail());
        userSip.setPhoneSip(request.getPhone());
        userSip.setDomainNameSip(domainName);
        userSip.setContextNameSip(contextName);
        userSip.setCodeSip(code);

       // String password = generateRandomPassword(12);
        //userSip.setPassword(password);

        UserSip savedUsersip = userSipRepository.save(userSip);

        HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = httpRequest.getHeader("Authorization");

        try {
            String password = UUID.randomUUID().toString().substring(0, 10);

            RegisterUserRequest userRequest = new RegisterUserRequest();
            userRequest.setEmail(request.getEmail());
            userRequest.setPassword(password);
            userRequest.setRole("USER_SIP");
            userRequest.setTenantId(tenant.getId());

            authServiceClient.registerUserSip(token, userRequest);

            emailService.sendCredentials(request.getEmail(), password);

        } catch (Exception e) {
            userSipRepository.delete(savedUsersip);
            throw new RuntimeException("Échec de la création de l'utilisateur sip. il a été supprimé.", e);
        }



    }

    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for(int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }


    public List<UserSip> getByTenant(Tenant tenant) {
        return userSipRepository.findAll()
                .stream().filter(u -> u.getTenant().equals(tenant))
                .collect(Collectors.toList());
    }

    public List<UserSip> getUsersByTenantId(Long tenantId) {
        return userSipRepository.findByTenantId(tenantId);
    }

    public Optional<UserSip> findByUsernameAndTenant(String username, Long tenantId) {
        return userSipRepository.findByUsernameAndTenantId(username, tenantId);
    }
}
