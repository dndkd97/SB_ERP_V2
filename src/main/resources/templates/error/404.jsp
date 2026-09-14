<%@taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
  <title>페이지를 찾을 수 없음: 404</title>
<!-- Latest compiled and minified CSS -->
<link
	href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
	rel="stylesheet">
<!-- Latest compiled JavaScript -->
<script
	src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
	<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">

  <style>
    body { background: #f8f9fa; }
    .error-wrap { min-height: 100vh; display: flex; align-items: center; justify-content: center; }
    .error-code { font-size: 6rem; font-weight: 700; color: #dee2e6; line-height: 1; }
    .error-icon { font-size: 3rem; color: #6c757d; }
  </style>
</head>
<body>
<div class="error-wrap">
  <div class="text-center px-3">
    <div class="error-code">404</div>
    <i class="ti ti-map-search error-icon mt-2"></i>
    <h1 class="fs-4 fw-semibold mt-3 mb-2">페이지를 찾을 수 없습니다</h1>
    <p class="text-muted mb-1">요청하신 주소가 존재하지 않거나 이동되었습니다.</p>
    <a href="${pageContext.request.contextPath}/" class="btn btn-primary mt-4">
      <i class="ti ti-home me-1"></i>홈으로 돌아가기
    </a>
  </div>
</div>
</body>
</html>