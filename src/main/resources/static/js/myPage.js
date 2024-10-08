import {putHeadersAccessToken} from "./jwt.js";

document.addEventListener("DOMContentLoaded", function () {
    const token = localStorage.getItem('accessToken');

    if (!token) {
        document.body.innerHTML = '';
        alert('로그인이 필요합니다. 로그인 페이지로 이동합니다.');
        window.location.href = '/html/login.html';
        return;
    }

    detail();

    const btnUpdateMember = document.querySelector("#btnUpdateMember");
    const btnAddressMember = document.querySelector("#btnAddressMember");
    const btnDeleteAccount = document.querySelector("#btnDeleteAccount");
    const btnMyPosts = document.querySelector("#btnMyPosts");

    if (btnUpdateMember) {
        btnUpdateMember.onclick = updateMember;
    }

    if (btnAddressMember) {
        btnAddressMember.onclick = updateAddress;
    }

    if (btnDeleteAccount) {
        btnDeleteAccount.onclick = deleteMember;
    }

    // 내 게시물 보러가기 버튼 클릭 시 세션 스토리지에 상태 저장
    btnMyPosts.onclick = function () {
        sessionStorage.setItem('viewMyPosts', 'true');
        window.location.href = '../html/boardList.html';
    };

});

async function detail() {
    let url = "/members/my-info";
    let response = await fetch(url, {
        method: 'GET',
        headers: putHeadersAccessToken()
    });
    let data = await response.json();

    if (data.code === "MEMBER-S002") {
        const email = data.data.email;
        const nickname = data.data.nickname;
        const street = data.data.address.street;
        const detail = data.data.address.detail;
        const zipcode = data.data.address.zipcode;

        document.querySelector("#email").value = email; // 닉네임 값을 칸에 넣기
        document.querySelector("#nickName").value = nickname; // 닉네임 값을 칸에 넣기
        document.querySelector("#street").value = street; // 닉네임 값을 칸에 넣기
        document.querySelector("#detail").value = detail; // 닉네임 값을 칸에 넣기
        document.querySelector("#zipcode").value = zipcode; // 닉네임 값을 칸에 넣기

        document.querySelector('.main-title').textContent = `${nickname}님의 상세페이지`;
    } else {
        alert("상세조회 과정에서 오류가 발생했습니다.");
    }
}

async function updateMember() {
    let nickName = document.querySelector("#nickName").value;

    let memberData = {
        nickname: nickName
    };

    try {
        // 사용자 정보 요청
        let response = await fetch('/members/my-info-update', {
            method: "PUT",
            headers: putHeadersAccessToken(),
            body: JSON.stringify(memberData)
        });

        // 응답 상태 확인
        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }

        // 응답 데이터 파싱
        let data = await response.json();
        console.log(data);

        // 응답 코드에 따른 처리
        if (data.code === "MEMBER-S003") { // 응답 코드 확인
            alert("회원 정보가 성공적으로 수정되었습니다.");

            // 로그아웃 요청
            await logoutAndRedirect();
        } else {
            alert(`회원 정보 수정 중 오류가 발생했습니다: ${data.message}`);
        }
    } catch (error) {
        console.error("Error:", error);
        alert("회원 정보 수정 중 오류가 발생했습니다: " + error.message);
    }
}

async function logoutAndRedirect() {
    try {
        // 로그아웃 요청
        let response = await fetch("/auth/logout", {
            method: "POST",
            headers: putHeadersAccessToken(),
        });

        if (!response.ok) {
            throw new Error(`Logout failed with status ${response.status}`);
        }

        // 로그아웃 성공 시 로컬 스토리지에서 토큰 제거
        localStorage.removeItem('accessToken');

        // 알림 후 메인 페이지로 리디렉션
        alert("로그아웃이 완료되었습니다.");
        window.location.href = "/html/login.html";  // 로그인 페이지로 리디렉션
    } catch (error) {
        console.error("Logout failed:", error);
        alert("로그아웃 중 오류가 발생했습니다: " + error.message);
    }
}


async function updateAddress() {
    let street = document.querySelector("#street").value;
    let detail = document.querySelector("#detail").value;
    let zipcode = document.querySelector("#zipcode").value;

    let memberData = {
        street, detail, zipcode
    };

    try {
        // 사용자 정보 요청
        let response = await fetch('/members/my-address-update', {
            method: "PUT",
            headers: putHeadersAccessToken(),
            body: JSON.stringify(memberData)
        });

        // 응답 상태 확인
        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }

        // 응답 데이터 파싱
        let data = await response.json();
        console.log(data);

        // 응답 코드에 따른 처리
        if (data.code === "MEMBER-S003") { // 응답 코드 확인
            alert("회원 정보가 성공적으로 수정되었습니다.");
        } else {
            alert(`회원 정보 수정 중 오류가 발생했습니다: ${data.message}`);
        }
    } catch (error) {
        console.error("Error:", error);
        alert("회원 정보 수정 중 오류가 발생했습니다: " + error.message);
    }
}

async function deleteMember() {
    if (confirm("정말로 회원 탈퇴를 진행하시겠습니까?")) {
        try {
            const response = await fetch('/members/my-info-delete', {
                method: 'DELETE',
                headers: putHeadersAccessToken()
            });

            // 응답 데이터를 JSON으로 변환
            let data = await response.json();

            // 응답 코드에 따른 처리
            if (data.code === "MEMBER-S004") {
                localStorage.removeItem('accessToken');
                alert("회원 탈퇴가 완료되었습니다.");
                window.location.href = '/index.html';  // 탈퇴 후 메인 페이지로 이동
            } else {
                alert(`회원 탈퇴 중 오류가 발생했습니다: ${data.message}`);
            }
        } catch (error) {
            alert("회원 탈퇴 중 오류가 발생했습니다: " + error.message);
        }
    }
}
