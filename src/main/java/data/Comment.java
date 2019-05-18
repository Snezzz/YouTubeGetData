package data;

import org.apache.commons.collections4.list.TreeList;

import java.util.List;

public class Comment {
    public String comment_id;
    String comment_author;
    String comment_author_name;
    String text;

    public List<Comment> getAnswers() {
        return answers;
    }

    public void setAnswers(List<Comment> answers) {
        this.answers = answers;
    }

    List<Comment> answers;
    List<String> answers_id;

    public void addAnswer(Comment answer){
        this.answers.add(answer);
    }
    public void addAnswer(String answer_id){
        this.answers_id.add(answer_id);
    }
    public List<String> getAnswers_id(){
        return this.answers_id;
    }
    public String getComment_id() {
        return comment_id;
    }

    public void setComment_id(String comment_id) {
        this.comment_id = comment_id;
    }

    public String getComment_author() {
        return comment_author;
    }

    public void setComment_author(String comment_author) {
        this.comment_author = comment_author;
    }

    public String getComment_author_name() {
        return comment_author_name;
    }

    public void setComment_author_name(String comment_author_name) {
        this.comment_author_name = comment_author_name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }



    public Comment(){
        this.answers_id = new TreeList<String>();
        this.answers = new TreeList<Comment>();
    }
    public Comment(String comment_id, String comment_author,
                   String comment_author_name, String text){
        this.comment_id = comment_id;
        this.comment_author = comment_author;
        this.text = text;
        this.comment_author_name = comment_author_name;
        this.answers_id = new TreeList<String>();
        this.answers = new TreeList<Comment>();
    }
}
