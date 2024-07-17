<!-- TODO: Add image of the bot here, make sure to place it on center -->
# <center>Mom Bot<center>

## Project's Ultimate Objective

The Ultimate objective of this project is to build a discord bot with codebase that is:
- **Concise**: remove unnessary task as much as possible
- **Resource Efficient**: use concurrency etc.
- **Light and Small in Size**: project sould be much small in size to deploye on ARM chip based computers


## Features as Objectives

1. **Notes Management**:`(Private Resposive feature)`
    1. Slash command to upload and download notes
    2. Use Google Drive API to upload documents to respective folder
    3. Download notes according to deatils as Ephemeral

2. **Profanity Handling**:
    1. **Explicit Text**:
        1. Identify abusive token
        2. warn user `(Public Resposive Feature)` & report to server owner
        3. kick if exceed limit & report to user an owner
    2. **Explicit Images**:
        1. Identify Image
        2. Delete image if explicit
        3. Report to User and server owner

3. **AI Features**:`(Public and Private Responsive Feature)`
    1. **Text generation**:
         1. Slash Command to send request
         2. API or Lib connectivity
         3. retrieve response as Ephemeral
    2. **Image generation**:
         1. Slash command to send request
         2. API or Lib connectivity
         3. retrieve image as Ephermal

4. **Goal Management**:TBD