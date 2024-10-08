package com.dangun.miniproject.member.service;

import com.dangun.miniproject.board.domain.Board;
import com.dangun.miniproject.comment.domain.Comment;
import com.dangun.miniproject.common.exception.InvalidInputException;
import com.dangun.miniproject.member.domain.Address;
import com.dangun.miniproject.member.domain.Member;
import com.dangun.miniproject.member.dto.GetAddressDto;
import com.dangun.miniproject.member.dto.GetMemberDto;
import com.dangun.miniproject.member.exception.MemberNotFoundException;
import com.dangun.miniproject.member.repository.AddressRepository;
import com.dangun.miniproject.member.repository.MemberRepository;
import com.dangun.miniproject.member.service.impl.MemberServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {
    @Mock
    private MemberRepository memberRepository;

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private MemberServiceImpl memberService;

    private Member member;
    private Address address;


    @BeforeEach
    public void setUp() {
        member = Member.builder()
                .email("email@example.com")
                .nickname("nickname")
                .build();

        address = Address.builder()
                .street("street")
                .detail("detail")
                .zipcode("00000")
                .member(member)
                .build();

        member.addAddress(address); // Member에 주소 추가
    }


    // 회원 조회 테스트
    @Test
    public void getMember() {
        // Given
        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(addressRepository.findById(member.getId())).thenReturn(Optional.of(address));

        // When
        GetMemberDto result = memberService.getMember(member.getId());

        // Then
        assertNotNull(result);
        assertEquals(member.getEmail(), result.getEmail());
        assertEquals(member.getNickname(), result.getNickname());

        assertNotNull(result.getAddress());
        assertEquals(address.getStreet(), result.getAddress().getStreet());
        assertEquals(address.getDetail(), result.getAddress().getDetail());
        assertEquals(address.getZipcode(), result.getAddress().getZipcode());
    }

    @Test
    public void getMember_NotFound() {
        // Given
        Long memberId = 1L;
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> memberService.getMember(memberId))
                .isInstanceOf(MemberNotFoundException.class)
                .hasMessage("Member not found");
    }


    // 본인 정보 조회 테스트
    @Test
    public void getMyInfo() {
        // Given
        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));

        // When
        GetMemberDto result = memberService.getMyInfo(member.getId());

        // Then
        assertNotNull(result);
        assertEquals(member.getEmail(), result.getEmail());
        assertEquals(member.getNickname(), result.getNickname());
        assertNotNull(result.getAddress());
        assertEquals(member.getAddress().getStreet(), result.getAddress().getStreet());
        assertEquals(member.getAddress().getDetail(), result.getAddress().getDetail());
        assertEquals(member.getAddress().getZipcode(), result.getAddress().getZipcode());
    }


    @Test
    void testUpdateMember_Success() {
        // Given
        Long memberId = 1L;
        GetMemberDto getMemberDto = GetMemberDto.builder()
                .nickname("newNickname")
                .build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        // When
        GetMemberDto updatedMemberDto = memberService.updateMember(getMemberDto, memberId);

        // Then
        assertNotNull(updatedMemberDto);
        assertEquals("newNickname", updatedMemberDto.getNickname());
        assertEquals(member.getEmail(), updatedMemberDto.getEmail());
        verify(memberRepository).save(member);
    }

    @Test
    void UpdateMember_ThrowsExceptionWhenNicknameIsNull() {
        // Given
        Long memberId = 1L;
        GetMemberDto getMemberDto = GetMemberDto.builder()
                .nickname(null) // 닉네임이 null
                .build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        // When & Then
        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            memberService.updateMember(getMemberDto, memberId);
        });

        assertEquals("닉네임을 입력해주세요.", exception.getMessage());
        verify(memberRepository, never()).save(any(Member.class)); // save가 호출되지 않아야 함
    }

    @Test
    void UpdateMember_ThrowsExceptionWhenNicknameIsBlank() {
        // Given
        Long memberId = 1L;
        GetMemberDto getMemberDto = GetMemberDto.builder()
                .nickname(" ") // 닉네임이 빈 공백 문자열
                .build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        // When & Then
        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            memberService.updateMember(getMemberDto, memberId);
        });

        assertEquals("닉네임을 입력해주세요.", exception.getMessage());
        verify(memberRepository, never()).save(any(Member.class)); // save가 호출되지 않아야 함
    }



    @Test
    void UpdateAddress_Success() {
        // Given
        Long addressId = 1L;
        GetAddressDto getAddressDto = GetAddressDto.builder()
                .street("new Street")
                .detail("new Detail")
                .zipcode("11111")
                .build();

        // AddressRepository.findById() 호출 시 address를 반환하도록 설정
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(address));

        // When
        GetAddressDto updatedAddressDto = memberService.updateAddress(getAddressDto, addressId);

        // Then
        assertNotNull(updatedAddressDto);
        assertEquals("new Street", updatedAddressDto.getStreet());
        assertEquals("new Detail", updatedAddressDto.getDetail());
        assertEquals("11111", updatedAddressDto.getZipcode());
        verify(addressRepository).save(address); // save가 호출되었는지 확인
    }

    @Test
    void UpdateAddress_ThrowsExceptionWhenStreetIsNull() {
        // Given
        Long addressId = 1L;
        GetAddressDto getAddressDto = GetAddressDto.builder()
                .street(null) // Street가 null
                .detail("Detail")
                .zipcode("00000")
                .build();

        when(addressRepository.findById(addressId)).thenReturn(Optional.of(address));

        // When & Then
        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            memberService.updateAddress(getAddressDto, addressId);
        });

        assertEquals("주소를 입력해주세요.", exception.getMessage());
        verify(addressRepository, never()).save(any(Address.class)); // save가 호출되지 않아야 함
    }

    @Test
    void UpdateAddress_ThrowsExceptionWhenDetailIsNull() {
        // Given
        Long addressId = 1L;
        GetAddressDto getAddressDto = GetAddressDto.builder()
                .street("Street")
                .detail(null) // Detail이 null
                .zipcode("00000")
                .build();

        when(addressRepository.findById(addressId)).thenReturn(Optional.of(address));

        // When & Then
        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            memberService.updateAddress(getAddressDto, addressId);
        });

        assertEquals("상세주소를 입력해주세요.", exception.getMessage());
        verify(addressRepository, never()).save(any(Address.class)); // save가 호출되지 않아야 함
    }

    @Test
    void UpdateAddress_ThrowsExceptionWhenZipcodeIsNull() {
        // Given
        Long addressId = 1L;
        GetAddressDto getAddressDto = GetAddressDto.builder()
                .street("Street")
                .detail("Detail")
                .zipcode(null) // Zipcode가 null
                .build();

        when(addressRepository.findById(addressId)).thenReturn(Optional.of(address));

        // When & Then
        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            memberService.updateAddress(getAddressDto, addressId);
        });

        assertEquals("우편번호를 입력해주세요.", exception.getMessage());
        verify(addressRepository, never()).save(any(Address.class)); // save가 호출되지 않아야 함
    }

    @Test
    void testUpdateAddress_ThrowsExceptionWhenStreetIsBlack() {
        // Given
        Long addressId = 1L;
        GetAddressDto getAddressDto = GetAddressDto.builder()
                .street("   ") // Street가 빈 공백 문자열
                .detail("Detail")
                .zipcode("00000")
                .build();

        when(addressRepository.findById(addressId)).thenReturn(Optional.of(address));

        // When & Then
        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            memberService.updateAddress(getAddressDto, addressId);
        });

        assertEquals("주소를 입력해주세요.", exception.getMessage());
        verify(addressRepository, never()).save(any(Address.class)); // save가 호출되지 않아야 함
    }

    @Test
    void testUpdateAddress_ThrowsExceptionWhenDetailIsBlack() {
        // Given
        Long addressId = 1L;
        GetAddressDto getAddressDto = GetAddressDto.builder()
                .street("Street")
                .detail("   ") // Detail이 빈 공백 문자열
                .zipcode("00000")
                .build();

        when(addressRepository.findById(addressId)).thenReturn(Optional.of(address));

        // When & Then
        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            memberService.updateAddress(getAddressDto, addressId);
        });

        assertEquals("상세주소를 입력해주세요.", exception.getMessage());
        verify(addressRepository, never()).save(any(Address.class)); // save가 호출되지 않아야 함
    }

    @Test
    void testUpdateAddress_ThrowsExceptionWhenZipcodeIsBlack() {
        // Given
        Long addressId = 1L;
        GetAddressDto getAddressDto = GetAddressDto.builder()
                .street("Street")
                .detail("Detail")
                .zipcode("   ") // Zipcode가 빈 공백 문자열
                .build();

        when(addressRepository.findById(addressId)).thenReturn(Optional.of(address));

        // When & Then
        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            memberService.updateAddress(getAddressDto, addressId);
        });

        assertEquals("우편번호를 입력해주세요.", exception.getMessage());
        verify(addressRepository, never()).save(any(Address.class)); // save가 호출되지 않아야 함
    }

    @Test
    void deleteMember() {
        // given
        setField(member, "comments", new ArrayList<Comment>());
        setField(member, "boards", new ArrayList<Board>());
        setField(member, "address", new Address());

        Comment comment = mock(Comment.class);
        Board board = mock(Board.class);

        List<Comment> comments = (List<Comment>) getField(member, "comments");
        comments.add(comment);

        List<Board> boards = (List<Board>) getField(member, "boards");
        boards.add(board);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        // when
        boolean result = memberService.deleteMember(1L);

        // then
        assertTrue(result);

        verify(memberRepository).deleteCommentsByMemberId(member.getId());
        verify(memberRepository).deleteBoardsByMemberId(member.getId());
        verify(addressRepository).delete(member.getAddress());
        verify(memberRepository).delete(member);
    }
}
