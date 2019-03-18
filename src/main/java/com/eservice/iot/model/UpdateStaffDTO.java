package com.eservice.iot.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class UpdateStaffDTO {

    /**
     * tag_id_list : ["string"]
     * person_information : {"birthday":"string","phone":"string","name":"string","remark":"string","id":"string","employed_date":"string"}
     * meta : {"additionalProp1":{},"additionalProp3":{},"additionalProp2":{}}
     * card_numbers : ["string"]
     * update_face : {"delete_face_id_list":["string"],"insert_face_image_content_list":["string"],"enforce":true}
     */
    @JsonProperty("tag_id_list")
    private List<String> tag_id_list;
    @JsonProperty("person_information")
    private PersonInformation person_information;
    @JsonProperty("meta")
    private Map meta;
    @JsonProperty("card_numbers")
    private List<String> card_numbers;
    @JsonProperty("update_face")
    private Update_faceEntity update_face;

    public void setTag_id_list(List<String> tag_id_list) {
        this.tag_id_list = tag_id_list;
    }

    public void setMeta(Map meta) {
        this.meta = meta;
    }

    public void setCard_numbers(List<String> card_numbers) {
        this.card_numbers = card_numbers;
    }

    public void setUpdate_face(Update_faceEntity update_face) {
        this.update_face = update_face;
    }

    public List<String> getTag_id_list() {
        return tag_id_list;
    }

    public Map getMeta() {
        return meta;
    }

    public List<String> getCard_numbers() {
        return card_numbers;
    }

    public Update_faceEntity getUpdate_face() {
        return update_face;
    }


    public PersonInformation getPerson_information() {
        return person_information;
    }

    public void setPerson_information(PersonInformation person_information) {
        this.person_information = person_information;
    }

    public class Update_faceEntity {
        /**
         * delete_face_id_list : ["string"]
         * insert_face_image_content_list : ["string"]
         * enforce : true
         */
        @JsonProperty("delete_face_id_list")
        private List<String> delete_face_id_list;
        @JsonProperty("insert_face_image_content_list")
        private List<String> insert_face_image_content_list;
        @JsonProperty("enforce")
        private boolean enforce;

        public void setDelete_face_id_list(List<String> delete_face_id_list) {
            this.delete_face_id_list = delete_face_id_list;
        }

        public void setInsert_face_image_content_list(List<String> insert_face_image_content_list) {
            this.insert_face_image_content_list = insert_face_image_content_list;
        }

        public void setEnforce(boolean enforce) {
            this.enforce = enforce;
        }

        public List<String> getDelete_face_id_list() {
            return delete_face_id_list;
        }

        public List<String> getInsert_face_image_content_list() {
            return insert_face_image_content_list;
        }

        public boolean isEnforce() {
            return enforce;
        }
    }
}
