package com.aritra.d.riad.CoWork.dto;

import java.util.List;
import lombok.Data;

@Data
public class MentorLeaderboardDTO {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String profilePicture;
    private boolean hasPublicProfile;
    private int totalRatings;      // Number of ratings received
    private double averageRating;  // Average rating
    private int aggregateRating;   // Sum of all ratings (can be negative)
    private int positiveRatings;   // Count of positive ratings (1-5)
    private int negativeRatings;   // Count of negative ratings (-2 to -1)
    private List<String> taskIds;  // List of task IDs this mentor is involved in
    private List<String> taskNames; // List of task names this mentor is involved in
    
    // Connection status for the current user with this mentor
    private String connectionStatus; // "none", "pending_sent", "pending_received", "connected"
}