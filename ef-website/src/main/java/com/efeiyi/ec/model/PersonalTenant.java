package com.efeiyi.ec.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;


/**
 * Created by IntelliJ IDEA.
 * User: ming
 * Date: 12-10-15
 * Time: 上午11:06
 * To change this template use File | Settings | File Templates.
 *
 *
 * 个人
 */
@Entity
@Table(name = "tenant_personal")
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"})
public class PersonalTenant extends BigTenant implements Serializable {

	private String identityPhotoUrl;//手持身份证照片;


	@Column(name = "identity_photo_url")
	public String getIdentityPhotoUrl() {
		return identityPhotoUrl;
	}

	public void setIdentityPhotoUrl(String identityPhotoUrl) {
		this.identityPhotoUrl = identityPhotoUrl;
	}
}
