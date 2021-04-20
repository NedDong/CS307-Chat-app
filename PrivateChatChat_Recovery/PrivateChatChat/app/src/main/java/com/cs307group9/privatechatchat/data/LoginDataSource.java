package com.cs307group9.privatechatchat.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.cs307group9.privatechatchat.data.model.LoggedInUser;

import java.io.IOException;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {

    private SharedPreferences sharedPreferences;

    public Result<LoggedInUser> login(String username, String password) {

        Context context;

        try {
            // TODO: handle loggedInUser authentication

            // sharedPreferences.getString("username", null);

            LoggedInUser fakeUser =
                    new LoggedInUser(
                            java.util.UUID.randomUUID().toString(),
                            username);
            return new Result.Success<>(fakeUser);
        } catch (Exception e) {
            return new Result.Error(new IOException("Error logging in", e));
        }
    }

    public void logout() {
        // TODO: revoke authentication
    }
}