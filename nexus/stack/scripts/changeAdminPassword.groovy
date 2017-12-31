def user = security.securitySystem.getUser('admin')
user.setEmailAddress('${SHATHEL_ENV_NEXUS_ADMIN_MAIL}')
security.securitySystem.updateUser(user)
security.securitySystem.changePassword('admin','${SHATHEL_ENV_NEXUS_ADMIN_PASS}')