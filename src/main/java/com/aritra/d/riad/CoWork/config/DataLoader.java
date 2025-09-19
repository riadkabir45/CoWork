package com.aritra.d.riad.CoWork.config;

import com.aritra.d.riad.CoWork.enumurator.TaskIntervalType;
import com.aritra.d.riad.CoWork.model.Tag;
import com.aritra.d.riad.CoWork.model.TaskInstances;
import com.aritra.d.riad.CoWork.model.Tasks;
import com.aritra.d.riad.CoWork.model.Users;
import com.aritra.d.riad.CoWork.service.ConnectionService;
import com.aritra.d.riad.CoWork.service.NotificationService;
import com.aritra.d.riad.CoWork.service.PermissionService;
import com.aritra.d.riad.CoWork.service.RoleService;
import com.aritra.d.riad.CoWork.service.TagService;
import com.aritra.d.riad.CoWork.service.TaskInstanceService;
import com.aritra.d.riad.CoWork.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.aritra.d.riad.CoWork.service.TasksService;
import com.aritra.d.riad.CoWork.service.TaskUpdatesService;


@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    @Autowired
    private RoleService roleService;
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private UserService userService;
    @Autowired
    private ConnectionService connectionService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private TasksService taskService;
    @Autowired
    private TaskInstanceService taskInstanceService;
    @Autowired
    private TaskUpdatesService taskUpdatesService;
    @Autowired
    private TagService tagService;

    @Override
    public void run(String... args) throws Exception {
        loadPermissions();
        loadRoles();
        userService.loadSupabaseUsers();
        loadUsers();
        loadTestTags();
        loadSampleTasks();
    }
    
    private void loadPermissions() {
        String[] permissions = {
            "READ_TASKS", "WRITE_TASKS", "DELETE_TASKS",
            "READ_USERS", "WRITE_USERS", "DELETE_USERS",
            "MANAGE_ROLES", "PROMOTE_MENTOR", "ASSIGN_MODERATOR",
            "BAN_USERS", "MODERATE_CONTENT"
        };
        
        for (String permissionName : permissions) {
            permissionService.createPermissionIfNotExists(permissionName, "Permission to " + permissionName.toLowerCase().replace("_", " "));
        }
    }
    
    private void loadRoles() {
        // Create UNREGISTERED role
        roleService.createRoleIfNotExists("UNREGISTERED", "Users who haven't completed registration", 
            new String[]{});
        
        // Create REGISTERED role
        roleService.createRoleIfNotExists("REGISTERED", "Registered users with basic access", 
            new String[]{"READ_TASKS", "WRITE_TASKS"});
        
        // Create MENTOR role
        roleService.createRoleIfNotExists("MENTOR", "Users who can mentor others", 
            new String[]{"READ_TASKS", "WRITE_TASKS", "READ_USERS"});
        
        // Create MODERATOR role
        roleService.createRoleIfNotExists("MODERATOR", "Users who can moderate content and manage users", 
            new String[]{"READ_TASKS", "WRITE_TASKS", "DELETE_TASKS", "READ_USERS", 
                        "WRITE_USERS", "BAN_USERS", "MODERATE_CONTENT"});
        
        // Create ADMIN role
        roleService.createRoleIfNotExists("ADMIN", "Administrators with full access", 
            new String[]{"READ_TASKS", "WRITE_TASKS", "DELETE_TASKS", "READ_USERS", 
                        "WRITE_USERS", "DELETE_USERS", "MANAGE_ROLES", "PROMOTE_MENTOR", 
                        "ASSIGN_MODERATOR", "BAN_USERS", "MODERATE_CONTENT"});
    }

    public void loadUsers() {
        // Only create users if they don't already exist
        if (userService.findByEmail("jan@example.com").isEmpty()) {
            userService.createUser("jan@example.com", "Jan", "Kowalski");
        }
        
        Users ruby;
        if (userService.findByEmail("ruby@example.com").isEmpty()) {
            ruby = userService.createUser("ruby@example.com", "Ruby", "Smith");
        } else {
            ruby = userService.findByEmail("ruby@example.com").orElseThrow();
        }
        
        if (userService.findByEmail("revere@example.com").isEmpty()) {
            userService.createUser("revere@example.com", "Revere", "Thee");
        }

        Users riad = userService.findByEmail("riadkabir45@gmail.com").orElseThrow();
        Users aritra = userService.findByEmail("aritra.chakraborty@g.bracu.ac.bd").orElseThrow();

        // Only assign admin role if user doesn't already have it
        if (!riad.hasRole("ADMIN")) {
            userService.assignAdmin(riad.getId());
        }
        if (!aritra.hasRole("ADMIN")) {
            userService.assignAdmin(aritra.getId());
        }
        
        // Only make riad mentor if not already a mentor
        if (!riad.hasRole("MENTOR")) {
            userService.promoteToMentor(riad);
        }
    }
    
    private void loadTestTags() {
        try {
            // Get admin user to create tags as
            Users adminUser = userService.findByEmail("riadkabir45@gmail.com").orElseThrow(
                () -> new RuntimeException("Admin user not found: riadkabir45@gmail.com")
            );
            log.info("Found admin user: {}", adminUser.getEmail());
            
            // Define comprehensive tags for different categories
            String[][] tagData = {
                // Sports & Fitness
                {"sports", "Physical sports activities", "#EF4444"},
                {"fitness", "General fitness and exercise", "#F97316"},
                {"cardio", "Cardiovascular exercises", "#DC2626"},
                {"strength", "Strength training and muscle building", "#B91C1C"},
                {"yoga", "Yoga and stretching activities", "#10B981"},
                {"running", "Running and jogging activities", "#059669"},
                
                // Health & Wellness
                {"health", "Health and wellness activities", "#06B6D4"},
                {"meditation", "Mindfulness and meditation", "#8B5CF6"},
                {"sleep", "Sleep improvement and hygiene", "#6366F1"},
                {"nutrition", "Diet and nutrition related", "#84CC16"},
                {"mental-health", "Mental health and wellbeing", "#EC4899"},
                
                // Education & Learning
                {"education", "Learning and educational activities", "#3B82F6"},
                {"reading", "Book reading and literature", "#1D4ED8"},
                {"language", "Language learning", "#7C3AED"},
                {"skills", "Skill development", "#0EA5E9"},
                {"coding", "Programming and software development", "#22C55E"},
                
                // Hobbies & Creative
                {"hobby", "Personal hobbies and interests", "#F59E0B"},
                {"creative", "Creative activities and arts", "#E11D48"},
                {"music", "Music practice and listening", "#9333EA"},
                {"art", "Drawing, painting, and visual arts", "#DB2777"},
                {"photography", "Photography and visual creation", "#0891B2"},
                
                // Productivity & Work
                {"productivity", "Productivity and efficiency", "#65A30D"},
                {"work", "Work and professional tasks", "#374151"},
                {"organization", "Organization and planning", "#6B7280"},
                {"focus", "Concentration and deep work", "#4B5563"},
                
                // Social & Relationships
                {"social", "Social activities and relationships", "#F472B6"},
                {"family", "Family time and activities", "#FB7185"},
                {"friends", "Activities with friends", "#FDA4AF"},
                
                // Daily Habits
                {"habit", "Daily habit building", "#A3A3A3"},
                {"routine", "Daily routines and consistency", "#737373"},
                {"self-care", "Personal care and grooming", "#FCA5A5"},
                
                // Test tag for development
                {"test", "Test tag for development", "#3B82F6"}
            };
            
            // Create tags if they don't exist
            for (String[] tagInfo : tagData) {
                String name = tagInfo[0];
                String description = tagInfo[1];
                String color = tagInfo[2];
                
                if (tagService.getTagByName(name).isEmpty()) {
                    Tag tag = tagService.createTag(name, description, color, adminUser);
                    log.info("Tag '{}' created successfully with ID: {}, approved: {}", name, tag.getId(), tag.isApproved());
                } else {
                    Tag existingTag = tagService.getTagByName(name).get();
                    log.info("Tag '{}' already exists with ID: {}, approved: {}", name, existingTag.getId(), existingTag.isApproved());
                }
            }
        } catch (Exception e) {
            log.error("Error creating tags: ", e);
        }
    }

    private void loadSampleTasks() {
        try {
            // Get users for task assignments
            Users riad = userService.findByEmail("riadkabir45@gmail.com").orElseThrow();
            Users ruby = userService.findByEmail("ruby@example.com").orElse(null);
            if (ruby == null) {
                log.warn("Ruby user not found, creating sample tasks only for riad");
            }

            // Define sample daily tasks with their associated tags
            Object[][] taskData = {
                // Fitness & Sports Tasks
                {"Daily Push-ups", "Complete 30 push-ups every day to build upper body strength", new String[]{"fitness", "strength", "habit"}},
                {"Morning Run", "Go for a 30-minute run to improve cardiovascular health", new String[]{"running", "cardio", "sports", "routine"}},
                {"Yoga Session", "Practice yoga for flexibility and mental wellness", new String[]{"yoga", "fitness", "meditation", "self-care"}},
                {"Marathon Training", "Long distance running preparation for marathon", new String[]{"running", "sports", "cardio", "fitness"}},
                {"Gym Workout", "Strength training session at the gym", new String[]{"strength", "fitness", "sports"}},
                
                // Health & Wellness Tasks
                {"Daily Meditation", "10-minute mindfulness meditation", new String[]{"meditation", "mental-health", "routine", "habit"}},
                {"Drink 8 Glasses of Water", "Stay hydrated throughout the day", new String[]{"health", "nutrition", "habit"}},
                {"8 Hours Sleep", "Maintain healthy sleep schedule", new String[]{"sleep", "health", "routine"}},
                {"Healthy Meal Prep", "Prepare nutritious meals for the day", new String[]{"nutrition", "health", "self-care"}},
                
                // Education & Learning Tasks
                {"Read 30 Pages", "Read 30 pages of a book daily", new String[]{"reading", "education", "habit"}},
                {"Learn Spanish", "Practice Spanish for 20 minutes", new String[]{"language", "education", "skills"}},
                {"Coding Practice", "Solve coding problems for skill improvement", new String[]{"coding", "skills", "education"}},
                {"Online Course", "Complete one lesson of online course", new String[]{"education", "skills"}},
                
                // Creative & Hobbies Tasks
                {"Draw Daily Sketch", "Create a daily sketch to improve art skills", new String[]{"art", "creative", "hobby", "habit"}},
                {"Practice Guitar", "30-minute guitar practice session", new String[]{"music", "hobby", "creative", "skills"}},
                {"Photography Walk", "Take photos during a nature walk", new String[]{"photography", "creative", "hobby"}},
                {"Write Journal", "Daily journaling for reflection", new String[]{"creative", "mental-health", "habit", "self-care"}},
                
                // Productivity & Work Tasks
                {"Organize Workspace", "Clean and organize desk and workspace", new String[]{"organization", "productivity", "work"}},
                {"Deep Work Session", "2-hour focused work session", new String[]{"focus", "productivity", "work"}},
                {"Review Daily Goals", "Plan and review daily objectives", new String[]{"productivity", "organization", "routine"}},
                {"Email Management", "Process and organize emails", new String[]{"productivity", "work", "organization"}},
                
                // Social & Family Tasks
                {"Call Family", "Weekly video call with family members", new String[]{"family", "social"}},
                {"Friend Hangout", "Spend quality time with friends", new String[]{"friends", "social"}},
                {"Community Service", "Volunteer for community activities", new String[]{"social", "habit"}},
                
                // Self-Care & Routine Tasks
                {"Morning Skincare", "Complete morning skincare routine", new String[]{"self-care", "routine", "health"}},
                {"Evening Reflection", "Reflect on the day's achievements", new String[]{"self-care", "mental-health", "routine"}},
                {"Declutter Space", "Organize and declutter living space", new String[]{"organization", "self-care", "routine"}}
            };

            // Create tasks and assign them to users
            for (Object[] taskInfo : taskData) {
                String title = (String) taskInfo[0];
                String description = (String) taskInfo[1];
                String[] tagNames = (String[]) taskInfo[2];

                // Check if task already exists
                if (taskService.findByTaskName(title).isEmpty()) {
                    // Create task
                    Tasks task = taskService.createTask(title, true);
                    task.setDescription(description);

                    // Add tags to task
                    for (String tagName : tagNames) {
                        tagService.getTagByName(tagName).ifPresent(tag -> {
                            task.addTag(tag);
                        });
                    }

                    // Save task with tags
                    taskService.createTaskInternal(task);
                    log.info("Created task: {} with {} tags", title, tagNames.length);

                    // Create task instances for users
                    if(Math.random() > 0.7) {
                        TaskInstances riadsTask = taskInstanceService.createTaskInstances(24, TaskIntervalType.HOURS, riad, task);
                        if (Math.random() > 0.5) {
                            taskUpdatesService.createTaskUpdates(riadsTask, String.valueOf((int)(Math.random()*100)), LocalDateTime.now().minusHours(2));
                        }
                    }

                    if (ruby != null && Math.random() > 0.3) {
                        TaskInstances rubysTask = taskInstanceService.createTaskInstances(24, TaskIntervalType.HOURS, ruby, task);
                        if (Math.random() > 0.6) {
                            taskUpdatesService.createTaskUpdates(rubysTask, "Great progress today!", LocalDateTime.now().minusHours(4));
                        }
                    }
                } else {
                    log.info("Task '{}' already exists, skipping creation", title);
                }
            }

            log.info("Sample tasks loading completed successfully");

        } catch (Exception e) {
            log.error("Error creating sample tasks: ", e);
        }
    }

}
