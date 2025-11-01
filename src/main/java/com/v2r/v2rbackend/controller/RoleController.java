package com.v2r.v2rbackend.controller;

import com.v2r.v2rbackend.dto.RoleUpdateDTO;
import com.v2r.v2rbackend.entity.Role;
import com.v2r.v2rbackend.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/roles")
@Tag(name = "Role Management", description = "APIs for managing roles")
public class RoleController {

    private final RoleService roleService;

    @Autowired
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    @Operation(summary = "Get all roles with pagination", description = "Retrieve a list of all roles with pagination support")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of roles")
    public ResponseEntity<Page<Role>> getAllRoles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Role> roles = roleService.getAllRoles(pageable);
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get role by ID", description = "Retrieve a specific role by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved role"),
            @ApiResponse(responseCode = "404", description = "Role not found")
    })
    public ResponseEntity<Object> getRoleById(
            @Parameter(description = "ID of the role to retrieve") @PathVariable Integer id) {
        return roleService.getRoleById(id)
                .<ResponseEntity<Object>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Role not found with id: " + id));
    }

    @GetMapping("/name/{roleName}")
    @Operation(summary = "Get role by name", description = "Retrieve a specific role by its name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved role"),
            @ApiResponse(responseCode = "404", description = "Role not found")
    })
    public ResponseEntity<Object> getRoleByName(
            @Parameter(description = "Name of the role to retrieve") @PathVariable String roleName) {
        return roleService.getRoleByName(roleName)
                .<ResponseEntity<Object>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Role not found with name: " + roleName));
    }

    @PostMapping
    @Operation(summary = "Create a new role", description = "Create a new role in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Role created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or role already exists")
    })
    public ResponseEntity<?> createRole(@RequestBody RoleUpdateDTO roleDTO) {
        try {
            Role role = new Role();
            role.setRoleName(roleDTO.getRoleName());
            Role createdRole = roleService.createRole(role);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdRole);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a role", description = "Update an existing role by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role updated successfully"),
            @ApiResponse(responseCode = "404", description = "Role not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<?> updateRole(
            @Parameter(description = "ID of the role to update") @PathVariable Integer id,
            @RequestBody RoleUpdateDTO roleDTO) {
        try {
            Role role = new Role();
            role.setRoleName(roleDTO.getRoleName());
            Role updatedRole = roleService.updateRole(id, role);
            return ResponseEntity.ok(updatedRole);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a role", description = "Delete a role by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Role deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Role not found")
    })
    public ResponseEntity<?> deleteRole(
            @Parameter(description = "ID of the role to delete") @PathVariable Integer id) {
        try {
            roleService.deleteRole(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/exists/{roleName}")
    @Operation(summary = "Check if role exists", description = "Check if a role with the given name exists")
    @ApiResponse(responseCode = "200", description = "Returns true if role exists, false otherwise")
    public ResponseEntity<Boolean> existsByRoleName(
            @Parameter(description = "Name of the role to check") @PathVariable String roleName) {
        boolean exists = roleService.existsByRoleName(roleName);
        return ResponseEntity.ok(exists);
    }
}
