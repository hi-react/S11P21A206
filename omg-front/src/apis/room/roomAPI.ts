import { END_POINT } from '@/apis/apiConstants';
import { axiosInstance } from '@/apis/axiosInstance';

interface InstanceError {
  message: string;
  response?: {
    status: number;
  };
}

// 대기방 생성
export const createWaitingRooms = async (userNickname: string) => {
  try {
    const response = await axiosInstance.post(
      `${END_POINT.ROOM}/create`,
      null,
      { params: { userNickname } },
    );
    return response.data.result;
  } catch (error: unknown) {
    const err = error as InstanceError;
    console.error(err.response?.status, err.message);
    throw error;
  }
};

//대기방 입장
