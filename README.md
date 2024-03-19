If you want to use this application, you have to register to firebase.
Then you have to create a firebase storage and a firebase database.
Database should contain those:

You have to add 4 different collection; families, family_tasks, member, member_tasks
each collections should contain this fields;
families: family_id, family_name, family_photo, family_pw
family_tasks: dueDate, family_id, task_approve, task_id, task_done, task_text
member: member_id, family_id, member_authority, member_name, member_photo, member_pw
member_tasks: m_dueDate, m_member_id, m_task_approve, m_task_id, m_task_done, m_task_text


