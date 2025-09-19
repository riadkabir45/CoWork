package com.aritra.d.riad.CoWork.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aritra.d.riad.CoWork.model.Tag;
import com.aritra.d.riad.CoWork.model.TagSuggestion;
import com.aritra.d.riad.CoWork.model.TaskTagSuggestion;
import com.aritra.d.riad.CoWork.model.Tasks;
import com.aritra.d.riad.CoWork.model.Users;
import com.aritra.d.riad.CoWork.repository.TagRepository;
import com.aritra.d.riad.CoWork.repository.TagSuggestionRepository;
import com.aritra.d.riad.CoWork.repository.TaskTagSuggestionRepository;
import com.aritra.d.riad.CoWork.repository.TasksRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TagService {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private TagSuggestionRepository tagSuggestionRepository;

    @Autowired
    private TaskTagSuggestionRepository taskTagSuggestionRepository;

    @Autowired
    private TasksRepository tasksRepository;

    // =============== TAG MANAGEMENT ===============

    /**
     * Get all approved tags
     */
    public List<Tag> getAllApprovedTags() {
        List<Tag> tags = tagRepository.findByApprovedTrue();
        log.info("Found {} approved tags in database", tags.size());
        return tags;
    }

    /**
     * Get popular tags (limited)
     */
    public List<Tag> getPopularTags(int limit) {
        List<Tag> popularTags = tagRepository.findPopularApprovedTags();
        return popularTags.size() > limit ? popularTags.subList(0, limit) : popularTags;
    }

    /**
     * Search tags by keyword
     */
    public List<Tag> searchTags(String keyword) {
        return tagRepository.findApprovedTagsByKeyword(keyword);
    }

    /**
     * Get tag by name (case insensitive)
     */
    public Optional<Tag> getTagByName(String name) {
        return tagRepository.findByNameIgnoreCase(name);
    }

    /**
     * Get tag by ID
     */
    public Optional<Tag> getTagById(String id) {
        return tagRepository.findById(id);
    }

    /**
     * Create a new tag (requires approval if not created by mentor/admin)
     */
    @Transactional
    public Tag createTag(String name, String description, String color, Users creator) {
        // Check if tag already exists
        if (tagRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Tag with name '" + name + "' already exists");
        }

        Tag tag = new Tag();
        tag.setName(name.toLowerCase().trim());
        tag.setDescription(description);
        tag.setColor(color != null ? color : "#3B82F6");
        tag.setCreator(creator);

        // Auto-approve if creator is mentor or admin
        if (creator.hasRole("MENTOR") || creator.hasRole("ADMIN") || creator.hasRole("MODERATOR")) {
            tag.approve(creator);
            log.info("Tag '{}' auto-approved for mentor/admin: {}", name, creator.getEmail());
        } else {
            log.info("Tag '{}' created by {} and pending approval", name, creator.getEmail());
        }

        return tagRepository.save(tag);
    }

    /**
     * Approve a pending tag
     */
    @Transactional
    public Tag approveTag(String tagId, Users approver) {
        Tag tag = tagRepository.findById(tagId)
            .orElseThrow(() -> new IllegalArgumentException("Tag not found"));

        if (tag.isApproved()) {
            throw new IllegalArgumentException("Tag is already approved");
        }

        if (!approver.hasRole("MENTOR") && !approver.hasRole("ADMIN") && !approver.hasRole("MODERATOR")) {
            throw new IllegalArgumentException("Only mentors, moderators, or admins can approve tags");
        }

        tag.approve(approver);
        Tag savedTag = tagRepository.save(tag);
        log.info("Tag '{}' approved by {}", tag.getName(), approver.getEmail());
        return savedTag;
    }

    // =============== TAG SUGGESTIONS ===============

    /**
     * Suggest a new tag
     */
    @Transactional
    public TagSuggestion suggestNewTag(String name, String description, String reason, Users suggester) {
        System.out.println("DEBUG: TagService.suggestNewTag called with name: " + name);
        
        // Check if tag already exists
        System.out.println("DEBUG: Checking if tag already exists...");
        if (tagRepository.existsByNameIgnoreCase(name)) {
            System.out.println("DEBUG: Tag already exists with name: " + name);
            throw new IllegalArgumentException("Tag with name '" + name + "' already exists");
        }
        System.out.println("DEBUG: Tag name is unique");

        // Check if user already has pending suggestion for this tag
        System.out.println("DEBUG: Checking for pending suggestions by user...");
        if (tagSuggestionRepository.existsPendingSuggestionByUserAndName(suggester.getId(), name)) {
            System.out.println("DEBUG: User already has pending suggestion for this tag");
            throw new IllegalArgumentException("You already have a pending suggestion for this tag name");
        }
        System.out.println("DEBUG: No pending suggestions found");

        System.out.println("DEBUG: Creating new TagSuggestion object...");
        TagSuggestion suggestion = new TagSuggestion();
        suggestion.setSuggestedName(name.toLowerCase().trim());
        suggestion.setDescription(description);
        suggestion.setReason(reason);
        suggestion.setSuggestedBy(suggester);

        System.out.println("DEBUG: Saving TagSuggestion...");
        TagSuggestion savedSuggestion = tagSuggestionRepository.save(suggestion);
        System.out.println("DEBUG: TagSuggestion saved with ID: " + savedSuggestion.getId());
        
        log.info("New tag suggestion '{}' created by {}", name, suggester.getEmail());
        return savedSuggestion;
    }

    /**
     * Get pending tag suggestions
     */
    public List<TagSuggestion> getPendingSuggestions() {
        return tagSuggestionRepository.findByStatusOrderByCreatedAtDesc(TagSuggestion.SuggestionStatus.PENDING);
    }

    /**
     * Approve a tag suggestion and create the tag
     */
    @Transactional
    public TagSuggestion approveSuggestion(String suggestionId, String comment, Users reviewer) {
        TagSuggestion suggestion = tagSuggestionRepository.findById(suggestionId)
            .orElseThrow(() -> new IllegalArgumentException("Tag suggestion not found"));

        if (suggestion.getStatus() != TagSuggestion.SuggestionStatus.PENDING) {
            throw new IllegalArgumentException("Suggestion is not pending");
        }

        if (!reviewer.hasRole("MENTOR") && !reviewer.hasRole("ADMIN") && !reviewer.hasRole("MODERATOR")) {
            throw new IllegalArgumentException("Only mentors, moderators, or admins can approve suggestions");
        }

        // Check if tag name still doesn't exist
        if (tagRepository.existsByNameIgnoreCase(suggestion.getSuggestedName())) {
            throw new IllegalArgumentException("Tag with this name now exists, cannot approve suggestion");
        }

        // Create the tag
        Tag newTag = new Tag();
        newTag.setName(suggestion.getSuggestedName());
        newTag.setDescription(suggestion.getDescription());
        newTag.setColor("#3B82F6"); // Default color
        newTag.setCreator(suggestion.getSuggestedBy());
        newTag.approve(reviewer); // Auto-approve since it's being created from approved suggestion

        Tag savedTag = tagRepository.save(newTag);

        // Update suggestion
        suggestion.approve(reviewer, comment, savedTag);
        TagSuggestion savedSuggestion = tagSuggestionRepository.save(suggestion);

        log.info("Tag suggestion '{}' approved by {} and tag created", 
                suggestion.getSuggestedName(), reviewer.getEmail());
        return savedSuggestion;
    }

    /**
     * Reject a tag suggestion
     */
    @Transactional
    public TagSuggestion rejectSuggestion(String suggestionId, String comment, Users reviewer) {
        TagSuggestion suggestion = tagSuggestionRepository.findById(suggestionId)
            .orElseThrow(() -> new IllegalArgumentException("Tag suggestion not found"));

        if (suggestion.getStatus() != TagSuggestion.SuggestionStatus.PENDING) {
            throw new IllegalArgumentException("Suggestion is not pending");
        }

        if (!reviewer.hasRole("MENTOR") && !reviewer.hasRole("ADMIN") && !reviewer.hasRole("MODERATOR")) {
            throw new IllegalArgumentException("Only mentors, moderators, or admins can reject suggestions");
        }

        suggestion.reject(reviewer, comment);
        TagSuggestion savedSuggestion = tagSuggestionRepository.save(suggestion);

        log.info("Tag suggestion '{}' rejected by {}", 
                suggestion.getSuggestedName(), reviewer.getEmail());
        return savedSuggestion;
    }

    // =============== TASK TAG SUGGESTIONS ===============

    /**
     * Suggest a tag for an existing task
     */
    @Transactional
    public TaskTagSuggestion suggestTagForTask(String tagId, String taskId, String reason, Users suggester) {
        Tag tag = tagRepository.findById(tagId)
            .orElseThrow(() -> new IllegalArgumentException("Tag not found"));

        Tasks task = tasksRepository.findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        if (!tag.isApproved()) {
            throw new IllegalArgumentException("Cannot suggest unapproved tags for tasks");
        }

        // Check if task already has this tag
        if (task.hasTag(tag)) {
            throw new IllegalArgumentException("Task already has this tag");
        }

        // Check if user already suggested this tag for this task
        if (taskTagSuggestionRepository.existsPendingSuggestionByTaskAndTagAndUser(taskId, tagId, suggester.getId())) {
            throw new IllegalArgumentException("You already have a pending suggestion for this tag on this task");
        }

        TaskTagSuggestion suggestion = new TaskTagSuggestion();
        suggestion.setTask(task);
        suggestion.setSuggestedTag(tag);
        suggestion.setSuggestedBy(suggester);
        suggestion.setReason(reason);

        TaskTagSuggestion savedSuggestion = taskTagSuggestionRepository.save(suggestion);
        log.info("Task tag suggestion created: tag '{}' for task '{}' by {}", 
                tag.getName(), task.getTaskName(), suggester.getEmail());
        return savedSuggestion;
    }

    /**
     * Get pending task tag suggestions
     */
    public List<TaskTagSuggestion> getPendingTaskTagSuggestions() {
        return taskTagSuggestionRepository.findByStatusOrderByCreatedAtDesc(TaskTagSuggestion.SuggestionStatus.PENDING);
    }

    /**
     * Approve a task tag suggestion
     */
    @Transactional
    public TaskTagSuggestion approveTaskTagSuggestion(String suggestionId, String comment, Users reviewer) {
        TaskTagSuggestion suggestion = taskTagSuggestionRepository.findById(suggestionId)
            .orElseThrow(() -> new IllegalArgumentException("Task tag suggestion not found"));

        if (suggestion.getStatus() != TaskTagSuggestion.SuggestionStatus.PENDING) {
            throw new IllegalArgumentException("Suggestion is not pending");
        }

        if (!reviewer.hasRole("MENTOR") && !reviewer.hasRole("ADMIN") && !reviewer.hasRole("MODERATOR")) {
            throw new IllegalArgumentException("Only mentors, moderators, or admins can approve suggestions");
        }

        // Check if task already has this tag (in case it was added meanwhile)
        if (suggestion.getTask().hasTag(suggestion.getSuggestedTag())) {
            throw new IllegalArgumentException("Task already has this tag");
        }

        // Approve suggestion (this will add tag to task)
        suggestion.approve(reviewer, comment);
        TaskTagSuggestion savedSuggestion = taskTagSuggestionRepository.save(suggestion);

        // Save the task with the new tag
        tasksRepository.save(suggestion.getTask());

        log.info("Task tag suggestion approved: tag '{}' added to task '{}' by {}", 
                suggestion.getSuggestedTag().getName(), 
                suggestion.getTask().getTaskName(), 
                reviewer.getEmail());
        return savedSuggestion;
    }

    /**
     * Reject a task tag suggestion
     */
    @Transactional
    public TaskTagSuggestion rejectTaskTagSuggestion(String suggestionId, String comment, Users reviewer) {
        TaskTagSuggestion suggestion = taskTagSuggestionRepository.findById(suggestionId)
            .orElseThrow(() -> new IllegalArgumentException("Task tag suggestion not found"));

        if (suggestion.getStatus() != TaskTagSuggestion.SuggestionStatus.PENDING) {
            throw new IllegalArgumentException("Suggestion is not pending");
        }

        if (!reviewer.hasRole("MENTOR") && !reviewer.hasRole("ADMIN") && !reviewer.hasRole("MODERATOR")) {
            throw new IllegalArgumentException("Only mentors, moderators, or admins can reject suggestions");
        }

        suggestion.reject(reviewer, comment);
        TaskTagSuggestion savedSuggestion = taskTagSuggestionRepository.save(suggestion);

        log.info("Task tag suggestion rejected: tag '{}' for task '{}' by {}", 
                suggestion.getSuggestedTag().getName(), 
                suggestion.getTask().getTaskName(), 
                reviewer.getEmail());
        return savedSuggestion;
    }

    // =============== UTILITY METHODS ===============

    /**
     * Get tags for a specific task
     */
    public List<Tag> getTagsForTask(String taskId) {
        return tagRepository.findTagsByTaskId(taskId);
    }

    /**
     * Get unapproved tags (for admin review)
     */
    public List<Tag> getUnapprovedTags() {
        return tagRepository.findByApprovedFalse();
    }
}