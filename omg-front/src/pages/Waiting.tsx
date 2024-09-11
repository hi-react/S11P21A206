import { useNavigate } from 'react-router-dom';

import ExitButton from '@/components/ExitButton';

export default function Waiting() {
  const navigate = useNavigate();

  const handleClick = () => {
    navigate('/game');
  };

  return (
    <div className='w-full flex justify-center items-center h-screen flex-col bg-lime-100 p-10 relative'>
      <div className='absolute right-1 bottom-1'>
        <ExitButton showText={true} />
      </div>

      <div className='w-full gap-10 flex h-full justify-center flex-col'>
        <h2 className='text-center text-omg-lg font-omg-body'>대기방</h2>
        <button className='bg-white text-omg-sm' onClick={handleClick}>
          게임 이동 버튼
        </button>
      </div>
    </div>
  );
}
