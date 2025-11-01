package com.v2r.v2rbackend.service.impl;

import com.v2r.v2rbackend.entity.Role;
import com.v2r.v2rbackend.repository.RoleRepository;
import com.v2r.v2rbackend.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Autowired
    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Override
    public Page<Role> getAllRoles(Pageable pageable) {
        return roleRepository.findAll(pageable);
    }

    @Override
    public Optional<Role> getRoleById(Integer roleID) {
        return roleRepository.findById(roleID);
    }

    @Override
    public Optional<Role> getRoleByName(String roleName) {
        return roleRepository.findByRoleName(roleName);
    }

    @Override
    public Role createRole(Role role) {
        if (roleRepository.existsByRoleName(role.getRoleName())) {
            throw new RuntimeException("Role with name '" + role.getRoleName() + "' already exists");
        }
        return roleRepository.save(role);
    }

    @Override
    public Role updateRole(Integer roleID, Role role) {
        // First check if role exists
        if (!roleRepository.existsById(roleID)) {
            throw new RuntimeException("Role not found with id: " + roleID);
        }

        // Check if the new role name already exists (excluding current role)
        roleRepository.findByRoleName(role.getRoleName()).ifPresent(existingRole -> {
            if (!existingRole.getRoleID().equals(roleID)) {
                throw new RuntimeException("Role with name '" + role.getRoleName() + "' already exists");
            }
        });

        // Set the ID to ensure we're updating the correct entity
        role.setRoleID(roleID);
        return roleRepository.save(role);
    }

    @Override
    public void deleteRole(Integer roleID) {
        if (!roleRepository.existsById(roleID)) {
            throw new RuntimeException("Role not found with id: " + roleID);
        }
        roleRepository.deleteById(roleID);
    }

    @Override
    public boolean existsByRoleName(String roleName) {
        return roleRepository.existsByRoleName(roleName);
    }
}
