public class DataSet {
    String username, userID, text, videoID, comment_id;
    DataSet(String videoID, String comment_id, String username, String userID, String text) {
        this.videoID = videoID;
        this.comment_id = comment_id;
        this.username = username;
        this.text = text;
        this.userID = userID;
    }
}
