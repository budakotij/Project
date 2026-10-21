INSERT INTO USERS (USER_NAME, NAME, GENDER, DATE_OF_BIRTH, PASSWORD_HASH, EMAIL, PROFILE_IMAGE, USER_TYPE, ROLES, REGISTRATION_DATE, PHONE_NUMBER)
VALUES (:userName, :name, :gender, :dateOfBirth, :passwordHash, :email,
        :profileImage, :userType, :roles, :registrationDate, :phoneNumber)