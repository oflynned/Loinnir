package com.syzible.loinnir.objects;

/**
 * Created by ed on 12/05/2017.
 */

public class FacebookUser {
    private String id, firstName, lastName, email, gender, profilePicURL;

    public FacebookUser(String id, String firstName, String lastName, String email, String gender, String profilePicURL) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.gender = gender;
        this.profilePicURL = profilePicURL;
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getGender() {
        return gender;
    }

    public String getProfilePicURL() {
        return profilePicURL;
    }
}
