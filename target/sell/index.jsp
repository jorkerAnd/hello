<%--
  Created by IntelliJ IDEA.
  User: sh joe
  Date: 2018/6/13
  Time: 16:27
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>joe</title>
</head>

<body>

springmvc上传文件
<form name="from1" action="/manage/product/upload.do" method="post" enctype="multipart/form-data">
<input type="file" name="upload_file">
<input type="submit" value="springmvc上传文件">

富文本上传
<form name="from2" action="/manage/product/richtext_img_upload.do" method="post" enctype="multipart/form-data">
<input type="file" name="upload_file">
<input type="submit" value="富文本的上传">
</form>
</body>
</html>
