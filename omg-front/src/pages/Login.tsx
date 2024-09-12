import { useNavigate } from 'react-router-dom';

export default function Login() {
  const navigate = useNavigate();

  const handleClick = () => {
    navigate('/waiting');
  };

  return (
    <div className='w-full flex justify-center items-center h-screen flex-col bg-yellow-50 p-10'>
      <div className='w-full gap-10 flex h-full justify-center flex-col'>
        <h2 className='text-center text-omg-lg font-omg-title'>
          로그인 페이지
        </h2>
        <button className='bg-white text-omg-sm' onClick={handleClick}>
          소셜로그인 버튼
        </button>
      </div>
    </div>
  );
}
