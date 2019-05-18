package data;

import org.apache.commons.collections4.list.TreeList;

import java.util.List;

public class Video {
    String header;
    String description;
    String channel_name;
    String channel_id;
    String date;
    String [] tags;
    int likes;
    int dislikes;
    int views;
    int comments_count;

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    List<Comment> comments;
    List<String> comment_id;

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public Video(){
    this.comment_id = new TreeList<String>();
    this.comments = new TreeList<Comment>();
}
   public Video(
                String header,String description,String channel_name,
                String channel_id, String [] tags, int likes,
                int dislikes, int comments_count,String date){
       this.header = header;
       this.description = description;
       this.channel_name = channel_name;
       this.channel_id = channel_id;
       this.tags = tags;
       this.likes = likes;
       this.dislikes = dislikes;
       this.comments_count = comments_count;
       this.date = date;
       this.comment_id = new TreeList<String>();
       this.comments = new TreeList<Comment>();
   }

   public void setComments(Comment comment){
     this.comments.add(comment);
   }
   public void addCommentId(String id){
        comment_id.add(id);
   }
   public List<String> getComment_id(){
        return this.comment_id;
   }



    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getChannel_name() {
        return channel_name;
    }

    public void setChannel_name(String channel_name) {
        this.channel_name = channel_name;
    }

    public String getChannel_id() {
        return channel_id;
    }

    public void setChannel_id(String channel_id) {
        this.channel_id = channel_id;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getDislikes() {
        return dislikes;
    }

    public void setDislikes(int dislikes) {
        this.dislikes = dislikes;
    }

    public int getComments_count() {
        return comments_count;
    }

    public void setComments_count(int comments_count) {
        this.comments_count = comments_count;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }



}
