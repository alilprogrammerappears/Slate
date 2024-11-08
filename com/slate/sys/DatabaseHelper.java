package com.slate.sys;

import com.slate.jda.EventPoll;
import com.slate.jda.UserPreferences;
import com.slate.jda.tasks.Task;
import com.slate.jda.tasks.TaskDraft;
import com.slate.jda.votepolls.VotePollData;
import com.slate.jda.votepolls.VotePollDraft;
import com.slate.jda.votepolls.VotePollOption;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DatabaseHelper {
    private static ConnectionPool connectionPool = ConnectionPool.createEmpty();

    private DatabaseHelper () {}

    // QUERIES
    public static Optional<UserPreferences> getUserPreferences (String userId) {
        Optional<Connection> connection = connectionPool.getConnection();
        if (connection.isEmpty()) { return Optional.empty(); }

        Optional<UserPreferences> userPreference = Optional.empty();
        try (PreparedStatement createPreferences = connection.get().prepareStatement(
            "INSERT INTO UserPreferences (userId) VALUES (?);");
            PreparedStatement getPreferences = connection.get().prepareStatement(
            "SELECT * FROM UserPreferences WHERE userId = ?;")) {
            createPreferences.setString(1, userId);
            getPreferences.setString(1, userId);

            try { createPreferences.executeUpdate(); }
            catch (Exception ignored) {}

            ResultSet results = getPreferences.executeQuery();
            results.next();
            userPreference = Optional.of(new UserPreferences(results.getString("userId"), results.getString("phoneNumber"),
                results.getString("emailAddress"), results.getBoolean("pingNotifications"),
                results.getBoolean("textNotifications"), results.getBoolean("emailNotifications")));

            results.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        connectionPool.release(connection.get());
        return userPreference;
    }
    public static boolean toggleNotification (String userId, String column) {
        Optional<Connection> connection = connectionPool.getConnection();
        if (connection.isEmpty()) { return false; }

        boolean success = false;
        try (PreparedStatement toggleNotification = connection.get().prepareStatement(
            "UPDATE UserPreferences SET " + column + " = NOT " + column + " WHERE userId = ?;")) {
            toggleNotification.setString(1, userId);

            if (toggleNotification.executeUpdate() > 0) {
                success = true;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            connectionPool.release(connection.get());
        }

        return success;
    }
    public static boolean updatePhoneNumber (String userId, String phoneNumber) {
        Optional<Connection> connection = connectionPool.getConnection();
        if (connection.isEmpty()) { return false; }

        boolean success = false;
        try (PreparedStatement updatePhone = connection.get().prepareStatement(
            "UPDATE UserPreferences SET phoneNumber = ? WHERE userId = ?;")) {
            updatePhone.setString(1, phoneNumber);
            updatePhone.setString(2, userId);

            if (updatePhone.executeUpdate() > 0) {
                success = true;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            connectionPool.release(connection.get());
        }

        return success;
    }
    public static boolean updateEmailAddress (String userId, String emailAddress) {
        Optional<Connection> connection = connectionPool.getConnection();
        if (connection.isEmpty()) { return false; }

        boolean success = false;
        try (PreparedStatement updateEmail = connection.get().prepareStatement(
            "UPDATE UserPreferences SET emailAddress = ? WHERE userId = ?;")) {
            updateEmail.setString(1, emailAddress);
            updateEmail.setString(2, userId);

            if (updateEmail.executeUpdate() > 0) {
                success = true;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            connectionPool.release(connection.get());
        }

        return success;
    }

    public static int addTask (TaskDraft task) {
        Optional<Connection> connection = connectionPool.getConnection();
        if (connection.isEmpty()) { return -1; }

        int taskId = -1;
        try (PreparedStatement addTask = connection.get().prepareStatement(
            "INSERT INTO Tasks (guildId, title, taskDescription, assignedRoleId, dueDate) VALUES (?, ?, ?, ?, ?);");
            PreparedStatement getTaskId = connection.get().prepareStatement(
            "SELECT LAST_INSERT_ID() as taskId;")) {
            addTask.setString(1, task.getGuildId());
            addTask.setString(2, task.getTitle());
            addTask.setString(3, task.getDescription());
            addTask.setString(4, task.getAssignedRoleId());
            addTask.setLong(5, task.getDueDateLong());

            if (addTask.executeUpdate() == 0) {
                throw new SQLException("Failed to add task");
            }

            ResultSet result = getTaskId.executeQuery();
            result.next();
            taskId = result.getInt(1);
            result.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            connectionPool.release(connection.get());
        }

        return taskId;
    }
    public static boolean completeTask (int taskId) {
        Optional<Connection> connection = connectionPool.getConnection();
        if (connection.isEmpty()) { return false; }

        boolean success = false;
        try (PreparedStatement setComplete = connection.get().prepareStatement(
            "UPDATE Tasks SET complete = TRUE WHERE taskId = ?;")) {
            setComplete.setInt(1, taskId);

            if (setComplete.executeUpdate() > 0) {
                success = true;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            connectionPool.release(connection.get());
        }

        return success;
    }
    public static Optional<Task> getTask (int taskId) {
        Optional<Connection> connection = connectionPool.getConnection();
        if (connection.isEmpty() || taskId == -1) { return Optional.empty(); }

        Optional<Task> task = Optional.empty();
        try (PreparedStatement getTask = connection.get().prepareStatement(
            "SELECT * FROM Tasks WHERE taskId = ?;")) {
            getTask.setInt(1, taskId);

            ResultSet results = getTask.executeQuery();
            if (results.isBeforeFirst()) {
                results.next();
                task = Optional.of(new Task(taskId, results.getString("guildId"), results.getString("title"),
                    results.getString("taskDescription"), results.getString("assignedRoleId"), results.getLong("dueDate"),
                    results.getBoolean("complete")));
            }
            results.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            connectionPool.release(connection.get());
        }

        return task;
    }

    public static int addVotePoll (VotePollDraft pollDraft) {
        int pollId = -1;
        Optional<Connection> connection = connectionPool.getConnection();
        if (connection.isEmpty()) { return pollId; }

        try (PreparedStatement addVotePoll = connection.get().prepareStatement(
            "INSERT INTO VotePoll (authorId, guildId, question, endDate) VALUES (?, ?, ?, ?)");
            PreparedStatement getPollId = connection.get().prepareStatement(
                "SELECT LAST_INSERT_ID() as pollId;");
            PreparedStatement addPollOptions = connection.get().prepareStatement(
                "INSERT INTO VotePollOption (pollId, emoji, optionDescription) VALUES (?, ?, ?);")) {
            connection.get().setAutoCommit(false);

            addVotePoll.setString(1, pollDraft.getAuthorId());
            addVotePoll.setString(2, pollDraft.getGuildId());
            addVotePoll.setString(3, pollDraft.getQuestion());
            addVotePoll.setLong(4, pollDraft.getEndDate().getEpochSecond());

            if (addVotePoll.executeUpdate() == 0) {
                throw new SQLException("Failed to insert base vote poll data");
            }

            ResultSet pollIdResult = getPollId.executeQuery();
            pollIdResult.next();
            pollId = pollIdResult.getInt(1);
            pollIdResult.close();

            addPollOptions.setInt(1, pollId);
            for (Map.Entry<String, String> entry : pollDraft.getOptions().entrySet()) {
                addPollOptions.setString(2, entry.getKey());
                addPollOptions.setString(3, entry.getValue());
                addPollOptions.addBatch();
            }
            addPollOptions.executeBatch();

            connection.get().commit();
            connection.get().setAutoCommit(true);
        }
        catch (Exception e1) {
            try {
                connection.get().rollback();
                connection.get().setAutoCommit(true);
            }
            catch (Exception e2) {
                e2.printStackTrace();
            }
            e1.printStackTrace();
        }
        finally {
            connectionPool.release(connection.get());
        }

        return pollId;
    }
    public static boolean removeVotePoll (int pollId) {
        boolean success = false;
        Optional<Connection> connection = connectionPool.getConnection();
        if (connection.isEmpty()) { return false; }

        try (PreparedStatement removePoll = connection.get().prepareStatement(
            "DELETE FROM VotePoll WHERE pollId = ?;")) {
            removePoll.setInt(1, pollId);

            if (removePoll.executeUpdate() > 0) {
                success = true;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            connectionPool.release(connection.get());
        }

        return success;
    }
    public static boolean updateDiscordIds (int pollId, String channelId, String messageId) {
        boolean success = false;
        Optional<Connection> connection = connectionPool.getConnection();
        if (connection.isEmpty()) { return false; }

        try (PreparedStatement updateDiscordIds = connection.get().prepareStatement(
            "UPDATE VotePoll SET channelId = ?, messageId = ? WHERE pollId = ?")) {
            updateDiscordIds.setString(1, channelId);
            updateDiscordIds.setString(2, messageId);
            updateDiscordIds.setInt(3, pollId);

            if (updateDiscordIds.executeUpdate() > 0) {
                success = true;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            connectionPool.release(connection.get());
        }

        return success;
    }
    public static Optional<VotePollData> getVotePollData (int pollId) {
        Optional<VotePollData> data = Optional.empty();
        Optional<Connection> connection = connectionPool.getConnection();
        if (connection.isEmpty() || pollId == -1) { return data; }

        try (PreparedStatement getVotePollData = connection.get().prepareStatement(
            "SELECT * FROM VotePoll WHERE pollId = ?;")) {
            getVotePollData.setInt(1, pollId);

            ResultSet result = getVotePollData.executeQuery();
            if (result.isBeforeFirst()) {
                result.next();
                data = Optional.of(new VotePollData(result.getInt("pollId"), result.getString("authorId"),
                    result.getString("guildId"), result.getString("question"),
                    Instant.ofEpochSecond(result.getLong("endDate")), result.getString("channelId"),
                    result.getString("messageId")));
            }
            result.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            connectionPool.release(connection.get());
        }

        return data;
    }
    public static Optional<VotePollOption> getVotePollOption (int optionId) {
        Optional<VotePollOption> option = Optional.empty();
        Optional<Connection> connection = connectionPool.getConnection();
        if (connection.isEmpty()) { return option; }

        try (PreparedStatement getOption = connection.get().prepareStatement(
            "SELECT * FROM VotePollOption WHERE optionId = ?;")) {
            getOption.setInt(1, optionId);

            ResultSet result = getOption.executeQuery();
            if (result.isBeforeFirst()) {
                result.next();
                option = Optional.of(new VotePollOption(optionId, result.getInt("pollId"), result.getString("emoji"),
                    result.getString("optionDescription"), -1));
            }
            result.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            connectionPool.release(connection.get());
        }

        return option;
    }
    public static List<VotePollOption> getVotePollOptions (int pollId) {
        List<VotePollOption> options = new ArrayList<>();
        Optional<Connection> connection = connectionPool.getConnection();
        if (connection.isEmpty()) { return options; }

        try (PreparedStatement getPollOptions = connection.get().prepareStatement(
            "SELECT vpo.*, CASE WHEN vpvc.totalVotes IS NULL THEN 0 ELSE vpvc.totalVotes END AS totalVotes " +
                "FROM VotePollOption AS vpo LEFT JOIN (SELECT pollId, optionId, COUNT(optionId) AS totalVotes " +
                "FROM VotePollVote WHERE pollId = ? GROUP BY optionId) AS vpvc ON vpo.pollId = vpvc.pollId " +
                "AND vpo.optionId = vpvc.optionId WHERE vpo.pollId = ?;")) {
            getPollOptions.setInt(1, pollId);
            getPollOptions.setInt(2, pollId);

            ResultSet results = getPollOptions.executeQuery();
            while (results.next()) {
                options.add(new VotePollOption(results.getInt("optionId"), results.getInt("pollId"),
                    results.getString("emoji"), results.getString("optionDescription"), results.getInt("totalVotes")));
            }
            results.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            connectionPool.release(connection.get());
        }

        return options;
    }
    public static Optional<VotePollOption> setUserVotePollVote (String voterId, int pollId, int optionId) {
        Optional<VotePollOption> votedOption = Optional.empty();
        Optional<Connection> connection = connectionPool.getConnection();
        if (connection.isEmpty()) { return votedOption; }

        try (PreparedStatement updateVote = connection.get().prepareStatement(
            "UPDATE VotePollVote SET optionId = ? WHERE voterId = ? AND pollId = ?;");
            PreparedStatement addVote = connection.get().prepareStatement(
                "INSERT INTO VotePollVote (voterId, pollId, optionId) VALUES (?, ?, ?)");
            PreparedStatement getOption = connection.get().prepareStatement(
                "SELECT * FROM VotePollOption WHERE optionId = ?;")) {
            updateVote.setInt(1, optionId);
            updateVote.setString(2, voterId);
            updateVote.setInt(3, pollId);

            addVote.setString(1, voterId);
            addVote.setInt(2, pollId);
            addVote.setInt(3, optionId);

            getOption.setInt(1, optionId);

            if (updateVote.executeUpdate() > 0 || addVote.executeUpdate() > 0) {
                ResultSet result = getOption.executeQuery();
                result.next();
                votedOption = Optional.of(new VotePollOption(optionId, result.getInt("pollId"), result.getString("emoji"),
                    result.getString("optionDescription"), -1));
                result.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            connectionPool.release(connection.get());
        }

        return votedOption;
    }
    public static List<Integer> getDueVotePolls (int secondBuffer) {
        List<Integer> pollIdList = new ArrayList<>();
        Optional<Connection> connection = connectionPool.getConnection();
        if (connection.isEmpty()) { return pollIdList; }

        try (PreparedStatement getDuePolls = connection.get().prepareStatement(
            "SELECT pollId FROM VotePoll WHERE (endDate - " + Instant.now().getEpochSecond() + ") <= ?")) {
            getDuePolls.setInt(1, secondBuffer);

            ResultSet results = getDuePolls.executeQuery();
            while (results.next()) {
                pollIdList.add(results.getInt(1));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            connectionPool.release(connection.get());
        }

        return pollIdList;
    }

    public static EventPoll addEvent(String eventName, String eventDescription, Date dateCreated, int voteTime,
                                     int confirmationsNeeded, Date option1, Date option2, Date option3, Date option4,
                                     Date option5, Boolean weekly, int daysBetweenReminders){
        Optional<Connection> connection = connectionPool.getConnection();
        if (connection.isEmpty()) {return null;}

        int eventID = -1;
        try (PreparedStatement addEvent = connection.get().prepareStatement("INSERT INTO EventPolls (" +
                "eventName, eventDescription, dateCreated, voteTime, confirmationsNeeded, " +
                "option1, option2, option3, option4, option5, weekly, daysBetweenReminders) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
                PreparedStatement getEventID = connection.get().prepareStatement(
                        "SELECT LAST_INSERT_ID() as eventID;")){

            addEvent.setString(1, eventName);
            addEvent.setString(2, eventDescription);
            addEvent.setDate(3, dateCreated);
            addEvent.setInt(4, voteTime);
            addEvent.setInt(5, confirmationsNeeded);
            addEvent.setDate(6, option1);
            addEvent.setDate(7, option2);
            addEvent.setDate(8, option3);
            addEvent.setDate(9, option4);
            addEvent.setDate(10, option5);
            if(weekly != null) {
                addEvent.setBoolean(11, weekly);
            }else{
                addEvent.setBoolean(11, false);
            }
            if(daysBetweenReminders != 0){
                addEvent.setInt(12, daysBetweenReminders);
            }else{
                addEvent.setInt(12, 0);
            }


            if(addEvent.executeUpdate() == 0){
                throw new SQLException("Failed to add event");
            }

            ResultSet result = getEventID.executeQuery();
            result.next();
            eventID = result.getInt(1);
            result.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            connectionPool.release(connection.get());
        }
        return new EventPoll(String.valueOf(eventID), eventName, eventDescription, dateCreated, voteTime,
                confirmationsNeeded, option1, option2, option3, option4, option5, weekly, daysBetweenReminders);
    }

    public static Boolean RecordVote(String memberID, String eventID, String option, Boolean response){
        Optional<Connection> connection = connectionPool.getConnection();
        if (connection.isEmpty()) { return null; }

        boolean success = false;

            try (
                    PreparedStatement updateVote = connection.get().prepareStatement(
                            "UPDATE EventResponses SET "+ option + " = ? WHERE eventID = ? AND memberID = ?;");
                    PreparedStatement addVote = connection.get().prepareStatement(
                            "INSERT INTO EventResponses (eventID, memberID, " + option + ") VALUES (?, ?, ?);");
            ) {
                updateVote.setBoolean(1, response);
                updateVote.setString(2, eventID);
                updateVote.setString(3, memberID);

                addVote.setString(1, eventID);
                addVote.setString(2, memberID);
                addVote.setBoolean(3, response);

                if (updateVote.executeUpdate() > 0 || addVote.executeUpdate() > 0) {
                    success = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                connectionPool.release(connection.get());
            }
        return success;
        }

    public static boolean updateEventName (String eventID, String newName) {
        Optional<Connection> connection = connectionPool.getConnection();
        if (connection.isEmpty()) { return false; }

        boolean success = false;
        try (PreparedStatement updateName = connection.get().prepareStatement(
                "UPDATE EventPolls SET eventName = ? WHERE eventID = ?;")) {
            updateName.setString(1, newName);
            updateName.setString(2, eventID);

            if (updateName.executeUpdate() > 0) {
                success = true;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            connectionPool.release(connection.get());
        }

        return success;
    }

    public static boolean updateEventDescription (String eventID, String newDescription) {
        Optional<Connection> connection = connectionPool.getConnection();
        if (connection.isEmpty()) { return false; }

        boolean success = false;
        try (PreparedStatement updateDescription = connection.get().prepareStatement(
                "UPDATE EventPolls SET eventDescription = ? WHERE eventID = ?;")) {
            updateDescription.setString(1, newDescription);
            updateDescription.setString(2, eventID);

            if (updateDescription.executeUpdate() > 0) {
                success = true;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            connectionPool.release(connection.get());
        }
        return success;
    }

    public static boolean updateEventVoteTime (String eventID, int voteTime) {
        Optional<Connection> connection = connectionPool.getConnection();
        if (connection.isEmpty()) { return false; }

        boolean success = false;
        try (PreparedStatement updateVoteTime = connection.get().prepareStatement(
                "UPDATE EventPolls SET voteTime = ? WHERE eventID = ?;")) {
            updateVoteTime.setInt(1, voteTime);
            updateVoteTime.setString(2, eventID);

            if (updateVoteTime.executeUpdate() > 0) {
                success = true;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            connectionPool.release(connection.get());
        }
        return success;
    }

    public static boolean updateEventReminders (String eventID, int reminderDays) {
        Optional<Connection> connection = connectionPool.getConnection();
        if (connection.isEmpty()) { return false; }

        boolean success = false;
        try (PreparedStatement updateReminders = connection.get().prepareStatement(
                "UPDATE EventPolls SET daysBetweenReminders = ? WHERE eventID = ?;")) {
            updateReminders.setInt(1, reminderDays);
            updateReminders.setString(2, eventID);

            if (updateReminders.executeUpdate() > 0) {
                success = true;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            connectionPool.release(connection.get());
        }
        return success;
    }

    public static boolean updateEventConfirmations (String eventID, int confirmations) {
        Optional<Connection> connection = connectionPool.getConnection();
        if (connection.isEmpty()) { return false; }

        boolean success = false;
        try (PreparedStatement updateConfirmations = connection.get().prepareStatement(
                "UPDATE EventPolls SET confirmationsNeeded = ? WHERE eventID = ?;")) {
            updateConfirmations.setInt(1, confirmations);
            updateConfirmations.setString(2, eventID);

            if (updateConfirmations.executeUpdate() > 0) {
                success = true;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            connectionPool.release(connection.get());
        }
        return success;
    }

    public static boolean updateWeekly (String eventID, Boolean weeklyStatus) {
        Optional<Connection> connection = connectionPool.getConnection();
        if (connection.isEmpty()) { return false; }

        boolean success = false;
        try (PreparedStatement updateWeekly = connection.get().prepareStatement(
                "UPDATE EventPolls SET weekly = ? WHERE eventID = ?;")) {
            updateWeekly.setBoolean(1, weeklyStatus);
            updateWeekly.setString(2, eventID);

            if (updateWeekly.executeUpdate() > 0) {
                success = true;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            connectionPool.release(connection.get());
        }
        return success;
    }

    public static boolean updateOption (String eventID, String optionNumber, Date newOption) {
        Optional<Connection> connection = connectionPool.getConnection();
        if (connection.isEmpty()) { return false; }

        boolean success = false;
        try (PreparedStatement updateOption = connection.get().prepareStatement(
                "UPDATE EventPolls SET "+optionNumber+" = ? WHERE eventID = ?;")) {
            updateOption.setDate(1, newOption);
            updateOption.setString(2, eventID);

            if (updateOption.executeUpdate() > 0) {
                success = true;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            connectionPool.release(connection.get());
        }
        return success;
    }

    public static boolean removeOption (String eventID, String optionNumber) {
        Optional<Connection> connection = connectionPool.getConnection();
        if (connection.isEmpty()) { return false; }

        boolean success = false;
        try (PreparedStatement updateOption = connection.get().prepareStatement(
                "UPDATE EventPolls SET "+optionNumber+" = null WHERE eventID = ?;")) {
            updateOption.setString(1, eventID);

            if (updateOption.executeUpdate() > 0) {
                success = true;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            connectionPool.release(connection.get());
        }
        return success;
    }

    public static boolean checkEventSuccess (String eventID, String option) {
        Optional<Connection> connection = connectionPool.getConnection();
        if (connection.isEmpty()) { return false; }

        boolean success = false;
        try (PreparedStatement checkEventSuccess = connection.get().prepareStatement(
                "SELECT SUM("+option+"),confirmationsNeeded FROM EventResponses r INNER JOIN EventPolls p " +
                     "ON r.eventID = p.eventID WHERE r.eventID = ? GROUP BY confirmationsNeeded;")) {
            checkEventSuccess.setString(1, eventID);

            ResultSet results = checkEventSuccess.executeQuery();
            results.next();
            int confirmations = results.getInt(1);
            int needed = results.getInt(2);

            if(confirmations == needed) {
                success = true;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            connectionPool.release(connection.get());
        }
        return success;
    }

    public static int checkReminders (String eventID) {
        Optional<Connection> connection = connectionPool.getConnection();
        if (connection.isEmpty()) { return -1; }

        int reminders = -1;
        try (PreparedStatement checkReminders = connection.get().prepareStatement(
                "SELECT daysBetweenReminders FROM EventPolls WHERE eventID = ?;")) {
            checkReminders.setString(1, eventID);

            ResultSet results = checkReminders.executeQuery();
            results.next();
            reminders = results.getInt(1);

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            connectionPool.release(connection.get());
        }
        return reminders;
    }

    public static ConnectionPool getPool () {
        return connectionPool;
    }

    public static void setConnectionPool (ConnectionPool connectionPool) {
        DatabaseHelper.connectionPool = connectionPool;
    }
}