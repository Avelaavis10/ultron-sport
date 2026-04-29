package za.co.ultronsport.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "athlete_profiles")
public class AthleteProfile extends BaseEntity {

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    private String sport;

    @Column(nullable = false)
    private String position;

    @Column(nullable = false)
    private Integer age;

    private String gender;
    private String location;
    private String schoolOrClub;

    @Column(length = 1200)
    private String bio;

    private Integer profileCompletenessScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationStatus verificationStatus;

    protected AthleteProfile() {
    }

    private AthleteProfile(Long userId, String sport, String position, Integer age, String gender,
                           String location, String schoolOrClub, String bio) {
        this.userId = userId;
        this.sport = sport;
        this.position = position;
        this.age = age;
        this.gender = gender;
        this.location = location;
        this.schoolOrClub = schoolOrClub;
        this.bio = bio;
        this.profileCompletenessScore = calculateProfileCompleteness();
        this.verificationStatus = VerificationStatus.PENDING_VERIFICATION;
    }

    public static AthleteProfile create(Long userId, String sport, String position, Integer age, String gender,
                                        String location, String schoolOrClub, String bio) {
        return new AthleteProfile(userId, sport, position, age, gender, location, schoolOrClub, bio);
    }

    public void markVerified() {
        verificationStatus = VerificationStatus.VERIFIED;
    }

    public Integer calculateProfileCompleteness() {
        int completed = 0;
        int total = 7;
        completed += userId != null ? 1 : 0;
        completed += hasText(sport) ? 1 : 0;
        completed += hasText(position) ? 1 : 0;
        completed += age != null ? 1 : 0;
        completed += hasText(location) ? 1 : 0;
        completed += hasText(schoolOrClub) ? 1 : 0;
        completed += hasText(bio) ? 1 : 0;
        return Math.round((completed * 100f) / total);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public Long getUserId() {
        return userId;
    }

    public String getSport() {
        return sport;
    }

    public String getPosition() {
        return position;
    }

    public Integer getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }

    public String getLocation() {
        return location;
    }

    public String getSchoolOrClub() {
        return schoolOrClub;
    }

    public String getBio() {
        return bio;
    }

    public Integer getProfileCompletenessScore() {
        return profileCompletenessScore;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }
}
