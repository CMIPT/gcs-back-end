package edu.cmipt.gcs.pojo.repository;

import edu.cmipt.gcs.pojo.user.UserPO;
import org.eclipse.jgit.lib.PersonIdent;

public record CommitAuthorVO(String id, String name, String email, String avatarUrl) {
  public CommitAuthorVO(UserPO userPO) {
    this(
        userPO.getId() == null ? null : userPO.getId().toString(),
        userPO.getUsername(),
        userPO.getEmail(),
        userPO.getAvatarUrl());
  }

  public CommitAuthorVO(PersonIdent personIdent) {
    this(null, personIdent.getName(), personIdent.getEmailAddress(), "");
  }
}
