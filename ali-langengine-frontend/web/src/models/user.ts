import { useState } from 'react';

export default function () {
  const [user, setUser] = useState<typeof window.user | null>(window.user); // 由后端注入到前端模版
  return { user, setUser };
}
