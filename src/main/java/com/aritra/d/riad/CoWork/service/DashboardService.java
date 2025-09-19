package com.aritra.d.riad.CoWork.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aritra.d.riad.CoWork.dto.CommunityUserSuggestionDTO;
import com.aritra.d.riad.CoWork.dto.DashboardUpdateDTO;
import com.aritra.d.riad.CoWork.dto.TagDTO;
import com.aritra.d.riad.CoWork.dto.TaskSuggestionDTO;
import com.aritra.d.riad.CoWork.model.TaskInstances;
import com.aritra.d.riad.CoWork.model.TaskUpdates;
import com.aritra.d.riad.CoWork.model.Tasks;
import com.aritra.d.riad.CoWork.model.Users;
import com.aritra.d.riad.CoWork.repository.TaskInstancesRepository;
import com.aritra.d.riad.CoWork.repository.TaskUpdatesRepository;
import com.aritra.d.riad.CoWork.repository.TasksRepository;
import com.aritra.d.riad.CoWork.repository.UsersRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DashboardService {

    @Autowired
    private TaskUpdatesRepository taskUpdatesRepository;
    
    @Autowired
    private TaskInstancesRepository taskInstancesRepository;
    
    @Autowired
    private TasksRepository tasksRepository;
    
    @Autowired
    private UsersRepository usersRepository;
    
    @Autowired
    private ConnectionService connectionService;

    public List<DashboardUpdateDTO> getRecentCommunityUpdates(String currentUserId) {
        try {
            // Get recent updates from all users for now (simplified)
            List<TaskUpdates> recentUpdates = taskUpdatesRepository.findAll().stream()
                .filter(update -> update.getUpdateTimestamp().isAfter(LocalDateTime.now().minusDays(7)))
                .sorted((a, b) -> b.getUpdateTimestamp().compareTo(a.getUpdateTimestamp()))
                .limit(10)
                .collect(Collectors.toList());
            
            return recentUpdates.stream()
                .map(this::mapToUpdateDTO)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("Error getting recent community updates: ", e);
            return new ArrayList<>();
        }
    }

    public List<CommunityUserSuggestionDTO> getCommunitySuggestions(String currentUserId) {
        try {
            // Get all users except current user (simplified)
            List<Users> suggestions = usersRepository.findAll().stream()
                .filter(user -> !user.getId().equals(currentUserId))
                .limit(5)
                .collect(Collectors.toList());
            
            return suggestions.stream()
                .map(user -> mapToCommunitySuggestionDTO(user, currentUserId))
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("Error getting community suggestions: ", e);
            return new ArrayList<>();
        }
    }

    public List<TaskSuggestionDTO> getTrendingTasks(String currentUserId) {
        try {
            // Get all tasks (simplified) - in production, filter by user not joined
            List<Tasks> trendingTasks = tasksRepository.findAll().stream()
                .limit(5)
                .collect(Collectors.toList());
            
            return trendingTasks.stream()
                .map(this::mapToTaskSuggestionDTO)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("Error getting trending tasks: ", e);
            return new ArrayList<>();
        }
    }

    private DashboardUpdateDTO mapToUpdateDTO(TaskUpdates update) {
        DashboardUpdateDTO dto = new DashboardUpdateDTO();
        dto.setId(update.getId());
        dto.setUpdateText(update.getUpdateDescription());
        dto.setTimestamp(update.getUpdateTimestamp());
        
        TaskInstances taskInstance = update.getTaskInstances();
        if (taskInstance != null) {
            dto.setTaskInstanceId(taskInstance.getId()); // Add task instance ID for comments
            Tasks task = taskInstance.getTask();
            Users user = taskInstance.getUser();
            
            if (task != null) {
                dto.setTaskName(task.getTaskName());
                
                // Map tags
                if (task.getTags() != null) {
                    List<TagDTO> tagDTOs = task.getTags().stream()
                        .map(tag -> new TagDTO(
                            tag.getId(),
                            tag.getName(),
                            tag.getDescription(),
                            tag.getColor(),
                            tag.isApproved()
                        ))
                        .collect(Collectors.toList());
                    dto.setTags(tagDTOs);
                }
            }
            
            if (user != null) {
                dto.setUserName(user.getFullName());
            }
        }
        
        return dto;
    }

    private CommunityUserSuggestionDTO mapToCommunitySuggestionDTO(Users user, String currentUserId) {
        CommunityUserSuggestionDTO dto = new CommunityUserSuggestionDTO();
        dto.setId(user.getId());
        dto.setName(user.getFullName());
        dto.setEmail(user.getEmail());
        
        // Calculate common tasks
        int commonTasks = calculateCommonTasks(user.getId(), currentUserId);
        dto.setCommonTasks(commonTasks);
        
        // Calculate mutual connections
        int mutualConnections = calculateMutualConnections(user.getId(), currentUserId);
        dto.setMutualConnections(mutualConnections);
        
        // Calculate active streak (simplified)
        int activeStreak = calculateActiveStreak(user.getId());
        dto.setActiveStreak(activeStreak);
        
        return dto;
    }

    private TaskSuggestionDTO mapToTaskSuggestionDTO(Tasks task) {
        TaskSuggestionDTO dto = new TaskSuggestionDTO();
        dto.setId(task.getId());
        dto.setTaskName(task.getTaskName());
        dto.setDescription(task.getDescription());
        
        // Map tags
        if (task.getTags() != null) {
            List<TagDTO> tagDTOs = task.getTags().stream()
                .map(tag -> new TagDTO(
                    tag.getId(),
                    tag.getName(),
                    tag.getDescription(),
                    tag.getColor(),
                    tag.isApproved()
                ))
                .collect(Collectors.toList());
            dto.setTags(tagDTOs);
        }
        
        // Calculate user count and completion rate (simplified)
        int userCount = (int) (Math.random() * 20) + 5; // 5-25 users
        dto.setUserCount(userCount);
        
        double completionRate = calculateCompletionRate(task.getId());
        dto.setCompletionRate(completionRate);
        
        return dto;
    }

    private int calculateCommonTasks(String userId1, String userId2) {
        // Simplified implementation
        return (int) (Math.random() * 5) + 1; // 1-5 common tasks
    }

    private int calculateMutualConnections(String userId1, String userId2) {
        // Simplified implementation
        return (int) (Math.random() * 3); // 0-2 mutual connections
    }

    private int calculateActiveStreak(String userId) {
        // Simplified implementation - calculate days with recent updates
        return (int) (Math.random() * 14) + 1; // 1-14 day streak
    }

    private double calculateCompletionRate(String taskId) {
        // Simplified implementation
        return Math.random() * 40 + 60; // 60-100% completion rate
    }
}