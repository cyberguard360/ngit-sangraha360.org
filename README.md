# Django REST Framework API

## Getting Started

1. Clone the repository


## Table of Contents
- [Installation](#installation)
- [Migrations](#migrations)
- [Running the Server](#running-the-server)
- [Features](#features)
  - [Login](#login)
  - [Register](#register)
  - [Add Data](#add-data)
  - [Predictions](#predictions)
  - [Add Token](#add-token)

## Installation

1. First make sure you have python and django installed.
2. Clone the repository.
3. Create a virtual environment using `python -m venv .venv`.
4. Activate the virtual environment. (On windows `call .venv\Scripts\activate`, on linux and mac `source .venv/bin/activate`).
5. Install the requirements using `pip install -r requirements.txt`.

## Migrations

Before running the server, you need to run the migrations. You can run the migrations using `python manage.py migrate`.

## Running the Server

After running the migrations, you can run the server using `python manage.py runserver`.

## Features

### Login

To login, use the `/login` endpoint and provide a valid username and password. The server will return a token, which is used in all other requests.

### Register

To register, use the `/register` endpoint and provide a valid username and password.

### Add Data

To add data, use the `/data` endpoint and provide a valid token and data. The data should be in the form of a csv file.

### Predictions

To get predictions, use the `/predictions` endpoint and provide a valid token.

### Add Token

To add a token, use the `/token` endpoint and provide a valid username and password. This will give you a token, which can be used in all other requests.

This is a basic explanation, you can find more details in the code and the API documentation.

