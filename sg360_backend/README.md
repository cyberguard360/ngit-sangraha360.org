
# Django Backend 🔰



Django REST Framework API facilitates user authentication and data management tasks. It allows users to register, login, and receive authentication tokens for subsequent requests. Additionally, it provides endpoints for uploading data in CSV format and obtaining predictions based on that data. Overall, the app serves as a platform for users to securely manage data and access predictive analysis features.
## Roadmap

- Project Setup

- User Authentication

- Data Management

- Predictive Analysis

- API Documentation

- Testing and Validation

- Deployment and Maintenance


## API Reference

#### POST all items

```http
  POST /register
```

| Parameter | Type     | Description                |
| :-------- | :------- | :------------------------- |
| `api_key` | `string` | **Required**. Your API key |



```http
  POST /login
```

| Parameter | Type     | Description                       |
| :-------- | :------- | :-------------------------------- |
| `id`      | `string` | **Required**. Id of item to fetch |



```http
  POST /project-data
```
| Parameter | Type     | Description                       |
| :-------- | :------- | :-------------------------------- |
| `data fields`   | `string` | **Required**. To fetch Predictions |


## Acknowledgements

 - [Django User Auth](https://github.com/boxabhi/)




## Install my-project with python

### Create Virtual Environment

```bash
  python -m venv venv
```

### Use Virtual Environment

```bash
  call .venv/Scripts/activate, on linux
  source .venv/bin/activate, on mac
  venv/Scripts/activate, on windows
```
    


### pip install requirements

```bash
  pip install -r requirements.txt
```

### migrate to database

```bash
  python manage.py makemigrations
  python manage.py migrate
```

### start server

```bash
  python manage.py runserver
```



