package com.v2r.v2rbackend.service;

import com.v2r.v2rbackend.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface RoleService {
    List<Role> getAllRoles();
    Page<Role> getAllRoles(Pageable pageable);
    Optional<Role> getRoleById(Integer roleID);
    Optional<Role> getRoleByName(String roleName);
    Role createRole(Role role);
    Role updateRole(Integer roleID, Role role);
    void deleteRole(Integer roleID);
    boolean existsByRoleName(String roleName);
}
