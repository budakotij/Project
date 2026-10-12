INSERT INTO USERS (USER_NAME, NAME, PASSWORD_HASH, EMAIL, PROFILE_IMAGE, USER_TYPE, ROLES, REGISTRATION_DATE)
VALUES (:userName, :name, :passwordHash, :email,
        :profileImage, :userType, :roles, :registrationDate)