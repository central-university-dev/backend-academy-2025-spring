package backend.academy.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import backend.academy.dto.UserProfile;
import org.springframework.stereotype.Service;

@Service
public class UserProfileService {
    private final List<UserProfile> profiles = new ArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public UserProfile createProfile(UserProfile profile) {
        profile.setId(idCounter.getAndIncrement());
        profiles.add(profile);
        return profile;
    }

    public Optional<UserProfile> getProfileById(Long id) {
        return profiles.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
    }

    public List<UserProfile> getProfiles(String name, String email, Integer age) {
        return profiles.stream()
                .filter(p -> name == null || p.getName().contains(name))
                .filter(p -> email == null || p.getEmail().contains(email))
                .filter(p -> age == null || p.getAge() == age)
                .toList();
    }

    public boolean updateProfile(Long id, UserProfile updatedProfile) {
        Optional<UserProfile> existingProfile = getProfileById(id);
        if (existingProfile.isPresent()) {
            UserProfile profile = existingProfile.get();
            profile.setName(updatedProfile.getName());
            profile.setEmail(updatedProfile.getEmail());
            profile.setAge(updatedProfile.getAge());
            return true;
        }
        return false;
    }

    public boolean deleteProfile(Long id) {
        return profiles.removeIf(p -> p.getId().equals(id));
    }
}
