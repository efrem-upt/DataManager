# DataManager

<p align="center">
  <img src="img/DataManager.png" width = "200" height = "200">
</p>

DataManager is an online data management web application made using Spring that allows users to view the services they interacted with by scanning their email and allowing them to send emails to services to get their online data erased.

## How it works

When a user signs up on DataManager, they will be prompted to connect their Google account. Once they do that, DataManager will scan the user's email using Google's gMail API and will create a list of the interactions they had with companies that the user can view. The user can then
choose a service they wish to remove their data from and press the button "Send email" so an email is sent from the associated Google account to the corresponding inbox of the company the user has selected that addresses this type of requests.

Because this service relies on a database of emails corresponding to companies, there is the chance that the service does not have the email for a corresponding company in the database. In this case, the user is presented with the choice to "Suggest an email".
When the user suggests an email, it is sent to a moderator that reviews it and approves or rejects the suggestions. If they approve it, all users who had that company in their interactions list
will be able to send an email to that company via DataManager, telling them that they want their data removed from their systems.

## Features
There are three roles: user, moderator and admin.

- Connect your Google account and scan your gmail. Connection can happen only after the user created an account on the service;
- Get a list of the websites you've used in the past based on your gmail account;
- Extract the list in PDF or Excel format;
- Suggest contact email for services whose email is not stored in the database;
- Submit emails via DataManager to the service you wish to delete your data from, the email will be sent on your behalf and from your gmail account.
- Moderators can view suggestions made by users and approve or reject them;
- Admin only has the power to delete users.
