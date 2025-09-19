package com.aritra.d.riad.CoWork.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aritra.d.riad.CoWork.dto.MentorLeaderboardDTO;
import com.aritra.d.riad.CoWork.dto.SimpleUserDTO;
import com.aritra.d.riad.CoWork.service.UserService;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping
    public List<SimpleUserDTO> getAllUsers() {
        return userService.getAllUsersDTO();
    }

    @GetMapping("/mentors/leaderboard")
    public List<MentorLeaderboardDTO> getMentorLeaderboard(
            @RequestParam(required = false) String taskFilter) {
        return userService.getMentorLeaderboard(taskFilter);
    }
}
