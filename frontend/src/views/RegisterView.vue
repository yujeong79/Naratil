
<!-- src/views/RegisterView.vue -->

<template>
    <div class="register-container">
      <div class="register-wrapper">
        <!-- 헤더 -->
        <div class="register-header">
          <img src="../assets/logo.jpg" alt="Logo" class="register-logo" />
          <h1 class="register-title">{{ appName }}</h1>
          <p class="register-subtitle">새로운 계정을 생성하여 서비스를 이용하세요</p>
        </div>
        
        <!-- 스텝 프로그레스 -->
        <el-steps :active="currentStep" finish-status="success" simple>
          <el-step title="계정 정보" />
          <el-step title="기업 정보" />
          <el-step title="관심 분야" />
        </el-steps>
        
        <!-- 회원가입 폼 -->
        <el-card class="register-form-card">
          <!-- 스텝 1: 계정 정보 -->
          <div v-if="currentStep === 1">
            <h2 class="step-title">계정 정보</h2>
            <p class="step-description">로그인에 사용할 계정 정보를 입력해주세요</p>
            
            <el-form
              ref="accountFormRef"
              :model="registerForm.account"
              :rules="accountRules"
              label-position="top"
            >
              <!-- 이메일 -->
              <el-form-item label="이메일" prop="email">
                <el-input
                  v-model="registerForm.account.email"
                  placeholder="이메일 주소"
                  type="email"
                  prefix-icon="Message"
                />
              </el-form-item>
              
              <!-- 비밀번호 -->
              <el-form-item label="비밀번호" prop="password">
                <el-input
                  v-model="registerForm.account.password"
                  placeholder="비밀번호 (6자 이상)"
                  type="password"
                  prefix-icon="Lock"
                  show-password
                />
              </el-form-item>
              
              <!-- 비밀번호 확인 -->
              <el-form-item label="비밀번호 확인" prop="confirmPassword">
                <el-input
                  v-model="registerForm.account.confirmPassword"
                  placeholder="비밀번호 확인"
                  type="password"
                  prefix-icon="Lock"
                  show-password
                />
              </el-form-item>
              
              <!-- 이름 -->
              <el-form-item label="이름" prop="name">
                <el-input
                  v-model="registerForm.account.name"
                  placeholder="이름"
                  prefix-icon="User"
                />
              </el-form-item>
              
              <!-- 전화번호 -->
              <el-form-item label="전화번호" prop="phone">
                <el-input
                  v-model="registerForm.account.phone"
                  placeholder="전화번호 (- 없이 입력)"
                  prefix-icon="Phone"
                />
              </el-form-item>
            </el-form>
            
            <div class="step-buttons">
              <el-button @click="navigateToLogin">취소</el-button>
              <el-button type="primary" @click="nextStep(1)">다음</el-button>
            </div>
          </div>
          
          <!-- 스텝 2: 기업 정보 -->
          <div v-if="currentStep === 2">
            <h2 class="step-title">기업 정보</h2>
            <p class="step-description">소속된 기업 정보를 입력해주세요</p>
            
            <el-form
              ref="companyFormRef"
              :model="registerForm.company"
              :rules="companyRules"
              label-position="top"
            >
            
              <!-- 사업자등록번호 -->
              <el-form-item label="사업자등록번호" prop="businessNumber">
                <el-input
                v-model="registerForm.company.businessNumber"
                placeholder="사업자등록번호 (- 없이 입력)"
                prefix-icon="Document"
                />
                <el-button @click="validateAndFetchBusinessData" type="primary">검증</el-button>
              </el-form-item>
              
              <!-- 기업명 -->
              <el-form-item label="기업명" prop="corpName">
                <el-input
                  v-model="registerForm.company.corpName"
                  placeholder="기업명"
                  prefix-icon="Office"
                />
              </el-form-item>
            
              <!-- 개업일 -->
              <el-form-item label="개업일" prop="openDate">
                <el-input
                  v-model="registerForm.company.openDate"
                  placeholder="개업일"
                  type="date"
                  prefix-icon="Calendar"
                />
              </el-form-item>

              <!-- CEO 이름 -->
              <el-form-item label="CEO 이름" prop="ceoName">
                <el-input
                  v-model="registerForm.company.ceoName"
                  placeholder="CEO 이름"
                  prefix-icon="User"
                />
              </el-form-item>

              <!-- 직원 수 -->
              <el-form-item label="직원 수" prop="emplyeNum">
                <el-input
                  v-model="registerForm.company.emplyeNum"
                  placeholder="직원 수"
                  type="number"
                  prefix-icon="User"
                />
              </el-form-item>

            </el-form>
            
            <div class="step-buttons">
              <el-button @click="prevStep">이전</el-button>
              <el-button type="primary" @click="nextStep(2)">다음</el-button>
            </div>
          </div>
          
          <!-- 스텝 3: 관심 분야 -->
          <div v-if="currentStep === 3">
            <h2 class="step-title">관심 분야</h2>
            <p class="step-description">관심 있는 분야를 선택하면 맞춤형 추천을 받을 수 있습니다</p>
            
            <el-form
              ref="interestsFormRef"
              :model="registerForm.interests"
              label-position="top"
            >
              <!-- 관심 키워드 -->
              <el-form-item label="관심 키워드">
                <el-tag
                  v-for="tag in registerForm.interests.keywords"
                  :key="tag"
                  closable
                  @close="removeKeyword(tag)"
                  class="interest-tag"
                >
                  {{ tag }}
                </el-tag>
                
                <el-input
                  v-if="inputVisible"
                  ref="keywordInputRef"
                  v-model="keywordInputValue"
                  class="keyword-input"
                  size="small"
                  @keyup.enter="addKeyword"
                  @blur="addKeyword"
                />
                
                <el-button
                  v-else
                  class="add-button"
                  size="small"
                  @click="showKeywordInput"
                >
                  + 키워드 추가
                </el-button>
              </el-form-item>
              
              <!-- 관심 지역 -->
              <el-form-item label="관심 지역">
                <el-select
                  v-model="registerForm.interests.regions"
                  multiple
                  collapse-tags
                  placeholder="관심 지역 선택"
                  style="width: 100%"
                >
                  <el-option label="서울" value="seoul" />
                  <el-option label="경기" value="gyeonggi" />
                  <el-option label="인천" value="incheon" />
                  <el-option label="부산" value="busan" />
                  <el-option label="대구" value="daegu" />
                  <el-option label="광주" value="gwangju" />
                  <el-option label="대전" value="daejeon" />
                  <el-option label="울산" value="ulsan" />
                  <el-option label="세종" value="sejong" />
                  <el-option label="강원" value="gangwon" />
                  <el-option label="충북" value="chungbuk" />
                  <el-option label="충남" value="chungnam" />
                  <el-option label="전북" value="jeonbuk" />
                  <el-option label="전남" value="jeonnam" />
                  <el-option label="경북" value="gyeongbuk" />
                  <el-option label="경남" value="gyeongnam" />
                  <el-option label="제주" value="jeju" />
                </el-select>
              </el-form-item>
              
              <!-- 관심 업종 -->
              <el-form-item label="관심 업종">
                <el-select
                  v-model="registerForm.interests.industries"
                  multiple
                  collapse-tags
                  placeholder="관심 업종 선택"
                  style="width: 100%"
                >
                  <el-option label="정보통신" value="IT" />
                  <el-option label="건설" value="CONST" />
                  <el-option label="제조" value="MANUF" />
                  <el-option label="용역" value="SERVICE" />
                  <el-option label="물품" value="GOODS" />
                  <el-option label="연구개발" value="RND" />
                </el-select>
              </el-form-item>
              
              <!-- 예산 범위 -->
              <el-form-item label="관심 예산 범위">
                <el-row :gutter="10">
                  <el-col :span="12">
                    <el-input
                      v-model="registerForm.interests.minBudget"
                      placeholder="최소 금액"
                      type="number"
                    >
                      <template #append>만원</template>
                    </el-input>
                  </el-col>
                  <el-col :span="12">
                    <el-input
                      v-model="registerForm.interests.maxBudget"
                      placeholder="최대 금액"
                      type="number"
                    >
                      <template #append>만원</template>
                    </el-input>
                  </el-col>
                </el-row>
              </el-form-item>
              
              <!-- 약관 동의 -->
              <el-form-item>
                <el-checkbox v-model="registerForm.interests.agreeTerms">
                  <span>서비스 <el-link type="primary" :underline="false">이용약관</el-link> 및 <el-link type="primary" :underline="false">개인정보처리방침</el-link>에 동의합니다</span>
                </el-checkbox>
              </el-form-item>
            </el-form>
            
            <div class="step-buttons">
              <el-button @click="prevStep">이전</el-button>
              <el-button type="primary" @click="submitRegister" :loading="loading">
                가입하기
              </el-button>
            </div>
          </div>
        </el-card>
        
        <!-- 로그인 링크 -->
        <div class="login-link">
          이미 계정이 있으신가요? <el-link type="primary" @click="navigateToLogin">로그인</el-link>
        </div>
      </div>
    </div>
  </template>
  

<!-- ======================================================================================================== -->
<!-- ======================================================================================================== -->
<!-- ======================================================================================================== -->




  <script setup>
  import axios from 'axios';

  import { ref, reactive, nextTick, inject, computed } from 'vue';
  import { useRouter } from 'vue-router';
  import { ElMessage } from 'element-plus';
  import { useUserStore } from '../stores/user';
  
  // 스토어 및 라우터
  const userStore = useUserStore();
  const router = useRouter();
  
  // 앱 이름 주입
  const appName = inject('appName', '나라장터 입찰정보 플랫폼');
  
  // 폼 참조
  const accountFormRef = ref(null);
  const companyFormRef = ref(null);
  const interestsFormRef = ref(null);
  const keywordInputRef = ref(null);
  
  // 상태
  const currentStep = ref(1);
  const inputVisible = ref(false);
  const keywordInputValue = ref('');
  const loading = computed(() => userStore.loading);
  
  // 폼 데이터
  const registerForm = reactive({
    account: {
      email: "",
      password: "",
      name: "",
      phone: ""
    },
    // company: {
    //   name: "",
    //   businessNumber: "",
    //   size: "",
    //   industry: "",
    //   address: ""
    // },
    company: {
      businessNumber: "",
      corpName: "",
      openDate: "",
      ceoName: "",
      emplyeNum: ""
    },
    interests: {
      keywords: [],
      regions: [],
      industries: [],
      minBudget: "",
      maxBudget: "",
      agreeTerms: false
    }
  });
  
  // 유효성 검사 규칙
  const accountRules = {
    email: [
      { required: true, message: '이메일을 입력해주세요', trigger: 'blur' },
      { type: 'email', message: '올바른 이메일 형식이 아닙니다', trigger: 'blur' }
    ],
    password: [
      { required: true, message: '비밀번호를 입력해주세요', trigger: 'blur' },
      { min: 6, message: '비밀번호는 최소 6자 이상이어야 합니다', trigger: 'blur' }
    ],
    confirmPassword: [
      { required: true, message: '비밀번호 확인을 입력해주세요', trigger: 'blur' },
      {
        validator: (rule, value, callback) => {
          if (value !== registerForm.account.password) {
            callback(new Error('비밀번호가 일치하지 않습니다'));
          } else {
            callback();
          }
        },
        trigger: 'blur'
      }
    ],
    name: [
      { required: true, message: '이름을 입력해주세요', trigger: 'blur' }
    ],
    phone: [
      { required: true, message: '전화번호를 입력해주세요', trigger: 'blur' },
      {
        pattern: /^\d{10,11}$/,
        message: '올바른 전화번호 형식이 아닙니다 (- 없이 숫자만 입력)',
        trigger: 'blur'
      }
    ]
  };
  
  const companyRules = {
  businessNumber: [
    { required: true, message: '사업자등록번호를 입력해주세요', trigger: 'blur' },
    {
      pattern: /^\d{10}$/,
      message: '올바른 사업자등록번호 형식이 아닙니다 (- 없이 10자리)',
      trigger: 'blur'
    }
  ],
  corpName: [
    { required: true, message: '기업명을 입력해주세요', trigger: 'blur' }
  ],
  openDate: [
    { required: true, message: '개업일을 입력해주세요', trigger: 'blur' }
  ],
  ceoName: [
    { required: true, message: 'CEO 이름을 입력해주세요', trigger: 'blur' }
  ],
  emplyeNum: [
    { required: true, message: '직원 수를 입력해주세요', trigger: 'blur' }
  ]
};

  
  // 메서드
  const nextStep = async (step) => {
    if (step === 1) {
      if (!accountFormRef.value) return;
      
      try {
        await accountFormRef.value.validate();
        currentStep.value = 2;
      } catch (error) {
        console.log('Validation failed', error);
      }
    } else if (step === 2) {
      if (!companyFormRef.value) return;
      
      try {
        await companyFormRef.value.validate();
        currentStep.value = 3;
      } catch (error) {
        console.log('Validation failed', error);
      }
    }
  };
  
  const prevStep = () => {
    currentStep.value--;
  };
  
  const showKeywordInput = () => {
    inputVisible.value = true;
    nextTick(() => {
      keywordInputRef.value.focus();
    });
  };
  
  const addKeyword = () => {
    if (keywordInputValue.value) {
      if (!registerForm.interests.keywords.includes(keywordInputValue.value)) {
        registerForm.interests.keywords.push(keywordInputValue.value);
      }
    }
    inputVisible.value = false;
    keywordInputValue.value = '';
  };
  
  const removeKeyword = (tag) => {
    registerForm.interests.keywords = registerForm.interests.keywords.filter(item => item !== tag);
  };
  
  const submitRegister = async () => {
  if (!registerForm.interests.agreeTerms) {
    ElMessage.warning('서비스 이용약관과 개인정보처리방침에 동의해주세요.');
    return;
  }

  const formData = {
    account: registerForm.account,
    company: registerForm.company,
    interests: registerForm.interests,
  };

  try {
    const response = await axios.post('http://localhost:8081/api/auth/signup', formData);
    console.log("api 요청 보냄")

    if (response.status === 200) {
      ElMessage.success('회원가입이 완료되었습니다.');
      router.push('/login');
    }
  } catch (error) {
    console.error('Registration failed:', error);
    ElMessage.error('회원가입에 실패했습니다. 다시 시도해주세요.');
  }
};


  
  const navigateToLogin = () => {
    router.push('/login');
  };


// 사업자 등록 API 메서드
const validationMessage = ref('');  // 결과 메시지 변수

let corpNm, opbizDt, emplyeNum, ceoNm; // 전역변수
// 사업자등록번호 API
const validateAndFetchBusinessData = async () => {
  console.log("사용자등록번호 버튼 눌렀음!!!!!!!");
  var businessNumber = registerForm.company.businessNumber;  // 사용자가 입력한 사업자등록번호

  if (!businessNumber) {
    validationMessage.value = "사업자등록번호를 입력해주세요.";  // 번호가 비어있다면 메시지
    return;
  }

  // let serviceKey = "2EQjYK7sdVTUmUq4l2FRtEwibiEigXeOhLj2Ky3gegUMSz30ykFNC8QxxUJUi97OCfoATLusc3WMGePjMQ8fFg%3D%3D";

  try {
    // API 통신 보내기
    const response = await axios({
      method: "get",
      url: `https://apis.data.go.kr/1230000/ao/UsrInfoService/getPrcrmntCorpBasicInfo`,
      params: {
      serviceKey: "2EQjYK7sdVTUmUq4l2FRtEwibiEigXeOhLj2Ky3gegUMSz30ykFNC8QxxUJUi97OCfoATLusc3WMGePjMQ8fFg==",  // 서비스 키
      pageNo: 1,               // 페이지 번호
      numOfRows: 10,           // 한 페이지에 보여줄 데이터 개수
      inqryDiv: 3,             // 조회 구분
      bizno: businessNumber,       // 사업자 번호
      type: 'json'             // 응답 데이터 형식
      }
    });

    console.log(response.status)

    // API 응답 성공
    if (response.status == 200) {
      console.log("if문 들어옴")

      console.log(response.data)
      
      console.log("휴휴휴휴")

      
      console.log(response.data.body)
      
      console.log("위는 바디 밑에는 바디 아이템즈")
      console.log(response.data.body.items[0])
      

      const data = response.data.body.items[0];  // 첫 번째 데이터 항목을 추출
      


      // 필요한 데이터 추출
      corpNm = data.corpNm;       // 회사명
      opbizDt = data.opbizDt;     // 사업 개시일
      emplyeNum = data.emplyeNum; // 직원 수
      ceoNm = data.ceoNm;         // CEO 이름

      registerForm.company.corpName = data.corpNm;       // 기업명
      registerForm.company.openDate = data.opbizDt;      // 개업일
      registerForm.company.emplyeNum = data.emplyeNum;   // 직원 수
      registerForm.company.ceoName = data.ceoNm;         // CEO 이름

      // 필요한 데이터 사용
      console.log("회사명:", corpNm);
      console.log("사업 개시일:", opbizDt);
      console.log("직원 수:", emplyeNum);
      console.log("CEO 이름:", ceoNm);

      validationMessage.value = "사업자 등록 정보가 존재합니다.";
    } else {
      validationMessage.value = "사업자 등록 정보가 존재하지 않습니다.";
    }
  } catch (error) {
    // API 응답 실패
    validationMessage.value = "API 요청에 실패했습니다.";
    console.error(error); // 에러 로그 출력
    console.log("제발 살려줘!!!");
  }
};

  


  </script>
  

  <!-- ======================================================================================================== -->
  <!-- ======================================================================================================== -->
  <!-- ======================================================================================================== -->
  <style scoped>
  .register-container {
    min-height: calc(100vh - 160px);
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 40px 20px;
    background-color: #f5f7fa;
  }
  
  .register-wrapper {
    max-width: 800px;
    width: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 30px;
  }
  
  .register-header {
    text-align: center;
    margin-bottom: 10px;
  }
  
  .register-logo {
    height: 60px;
    margin-bottom: 16px;
  }
  
  .register-title {
    font-size: 2rem;
    margin: 0;
    color: #409EFF;
  }
  
  .register-subtitle {
    font-size: 1.1rem;
    color: #606266;
    margin-top: 8px;
  }
  
  .register-form-card {
    width: 100%;
    padding: 10px;
  }
  
  .step-title {
    font-size: 1.5rem;
    margin: 0 0 8px 0;
    color: #303133;
  }
  
  .step-description {
    margin-top: 0;
    margin-bottom: 24px;
    color: #909399;
    font-size: 0.9rem;
  }
  
  .step-buttons {
    display: flex;
    justify-content: space-between;
    margin-top: 24px;
  }
  
  .login-link {
    text-align: center;
    margin-top: 16px;
    font-size: 0.9rem;
    color: #606266;
  }
  
  .interest-tag {
    margin-right: 8px;
    margin-bottom: 8px;
  }
  
  .keyword-input {
    width: 120px;
    margin-right: 8px;
    vertical-align: bottom;
  }
  
  .add-button {
    margin-top: 8px;
  }
  
  @media (max-width: 768px) {
    .register-wrapper {
      gap: 20px;
    }
    
    .register-title {
      font-size: 1.6rem;
    }
    
    .register-logo {
      height: 50px;
    }
  }
  </style>